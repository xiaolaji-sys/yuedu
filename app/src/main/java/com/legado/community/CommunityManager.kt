package com.legado.community

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 社区功能管理器 - 书评分享、推荐系统、用户互动
 */
@Singleton
class CommunityManager @Inject constructor(
    private val context: Context,
    private val apiService: CommunityApi,
    private val localRepository: CommunityLocalRepository,
    private val recommendationEngine: RecommendationEngine
) {

    /**
     * 获取书籍推荐
     */
    suspend fun getBookRecommendations(
        userId: String,
        bookCategories: List<String> = emptyList(),
        readingHistory: List<String> = emptyList()
    ): List<BookRecommendation> {
        return try {
            // 1. 本地推荐引擎
            val localRecs = recommendationEngine.generateRecommendations(
                userId, bookCategories, readingHistory
            )

            // 2. 云端协同过滤
            val cloudRecs = apiService.getCollaborativeRecommendations(userId)

            // 3. 混合推荐结果
            combineRecommendations(localRecs, cloudRecs)
        } catch (e: Exception) {
            println("获取推荐失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 发布书评
     */
    suspend fun publishReview(
        userId: String,
        bookId: String,
        rating: Float,
        title: String,
        content: String,
        tags: List<String> = emptyList()
    ): ReviewResult {
        return try {
            val review = CommunityReview(
                id = generateReviewId(),
                userId = userId,
                bookId = bookId,
                rating = rating,
                title = title,
                content = content,
                tags = tags,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // 1. 保存到本地
            localRepository.saveReview(review)

            // 2. 同步到云端
            apiService.publishReview(review)

            // 3. 更新推荐数据
            recommendationEngine.updateUserPreferences(userId, review)

            ReviewResult.Success(review.id)
        } catch (e: Exception) {
            ReviewResult.Failure(e.message ?: "未知错误")
        }
    }

    /**
     * 获取书籍讨论
     */
    suspend fun getBookDiscussion(bookId: String): DiscussionThread {
        return try {
            // 1. 获取本地缓存的讨论
            val localDiscussions = localRepository.getBookDiscussions(bookId)

            // 2. 从云端获取最新更新
            val cloudUpdates = apiService.getBookDiscussion(bookId)

            // 3. 合并讨论内容
            DiscussionThread(
                bookId = bookId,
                threads = mergeDiscussionThreads(localDiscussions, cloudUpdates),
                totalReplies = localDiscussions.sumOf { it.replies.size } + cloudUpdates.sumOf { it.replies.size },
                lastUpdated = maxOf(
                    localDiscussions.maxOfOrNull { it.lastReplyTime } ?: 0L,
                    cloudUpdates.maxOfOrNull { it.lastReplyTime } ?: 0L
                )
            )
        } catch (e: Exception) {
            DiscussionThread.Empty(bookId)
        }
    }

    /**
     * 搜索社区内容
     */
    suspend fun searchCommunityContent(query: String, filters: SearchFilters): SearchResults {
        return try {
            // 1. 本地搜索
            val localResults = localRepository.searchContent(query, filters)

            // 2. 云端搜索
            val cloudResults = apiService.searchCommunityContent(query, filters)

            // 3. 合并和排序结果
            SearchResults(
                reviews = mergeReviews(localResults.reviews, cloudResults.reviews),
                discussions = mergeDiscussions(localResults.discussions, cloudResults.discussions),
                books = mergeBooks(localResults.books, cloudResults.books),
                users = mergeUsers(localResults.users, cloudResults.users),
                totalCount = localResults.totalCount + cloudResults.totalCount
            )
        } catch (e: Exception) {
            SearchResults.Empty(query)
        }
    }

    /**
     * 获取热门话题
     */
    fun getTrendingTopics(): List<TrendingTopic> {
        return listOf(
            TrendingTopic("玄幻", 15420, "#玄幻小说#"),
            TrendingTopic("都市", 12800, "#都市小说#"),
            TrendingTopic("历史", 9650, "#历史小说#"),
            TrendingTopic("科幻", 7200, "#科幻小说#"),
            TrendingTopic("言情", 6800, "#言情小说#")
        )
    }

    /**
     * 创建阅读挑战
     */
    suspend fun createReadingChallenge(
        userId: String,
        challenge: ReadingChallenge
    ): ChallengeResult {
        return try {
            val createdChallenge = apiService.createChallenge(challenge.copy(
                creatorId = userId,
                createdAt = System.currentTimeMillis()
            ))

            ChallengeResult.Success(createdChallenge.id)
        } catch (e: Exception) {
            ChallengeResult.Failure(e.message ?: "创建失败")
        }
    }

    /**
     * 加入阅读挑战
     */
    suspend fun joinChallenge(challengeId: String, userId: String): JoinResult {
        return try {
            val result = apiService.joinChallenge(challengeId, userId)
            if (result.success) {
                JoinResult.Success(result.challengeProgress)
            } else {
                JoinResult.Failure(result.errorMessage ?: "加入失败")
            }
        } catch (e: Exception) {
            JoinResult.Failure(e.message ?: "网络错误")
        }
    }

    private fun combineRecommendations(
        localRecs: List<BookRecommendation>,
        cloudRecs: List<BookRecommendation>
    ): List<BookRecommendation> {
        val combined = mutableMapOf<String, BookRecommendation>()
        
        // 添加本地推荐
        localRecs.forEach { rec ->
            combined[rec.bookId] = rec.copy(source = "local")
        }
        
        // 添加云端推荐，如果冲突则取评分更高的
        cloudRecs.forEach { rec ->
            val existing = combined[rec.bookId]
            if (existing == null || rec.score > existing.score) {
                combined[rec.bookId] = rec.copy(source = "cloud")
            }
        }
        
        return combined.values.sortedByDescending { it.score }.take(20)
    }

    private fun mergeDiscussionThreads(
        local: List<DiscussionThread>,
        cloud: List<DiscussionThread>
    ): List<DiscussionThread> {
        val merged = mutableListOf<DiscussionThread>()
        val allThreads = (local + cloud).associateBy { it.threadId }
        
        return allThreads.values.sortedByDescending { it.lastReplyTime }
    }

    private fun mergeReviews(local: List<CommunityReview>, cloud: List<CommunityReview>): List<CommunityReview> {
        val merged = mutableMapOf<String, CommunityReview>()
        
        (local + cloud).forEach { review ->
            val existing = merged[review.id]
            if (existing == null || review.createdAt > existing.createdAt) {
                merged[review.id] = review
            }
        }
        
        return merged.values.sortedByDescending { it.createdAt }
    }

    private fun generateReviewId(): String {
        return "review_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

// Data Classes
data class BookRecommendation(
    val bookId: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val score: Float,
    val reason: String,
    val source: String, // "local" or "cloud"
    val tags: List<String>
)

data class CommunityReview(
    val id: String,
    val userId: String,
    val bookId: String,
    val rating: Float,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val likes: Int = 0,
    val replies: List<ReviewReply> = emptyList()
)

data class ReviewReply(
    val id: String,
    val userId: String,
    val content: String,
    val createdAt: Long,
    val likes: Int = 0
)

data class DiscussionThread(
    val threadId: String,
    val bookId: String,
    val title: String,
    val content: String,
    val authorId: String,
    val createdAt: Long,
    val lastReplyTime: Long,
    val replies: List<DiscussionReply>,
    val tags: List<String> = emptyList()
) {
    companion object {
        fun Empty(bookId: String) = DiscussionThread(
            threadId = "empty_$bookId",
            bookId = bookId,
            title = "暂无讨论",
            content = "",
            authorId = "",
            createdAt = 0L,
            lastReplyTime = 0L,
            replies = emptyList()
        )
    }
}

data class DiscussionReply(
    val id: String,
    val threadId: String,
    val userId: String,
    val content: String,
    val createdAt: Long,
    val likes: Int = 0
)

data class SearchFilters(
    val contentType: Set<ContentType> = setOf(ContentType.REVIEW, ContentType.DISCUSSION),
    val minRating: Float = 0f,
    val dateRange: DateRange? = null,
    val tags: List<String> = emptyList()
)

enum class ContentType {
    REVIEW, DISCUSSION, BOOK, USER
}

data class DateRange(
    val start: Long,
    val end: Long
)

data class SearchResults(
    val reviews: List<CommunityReview>,
    val discussions: List<DiscussionThread>,
    val books: List<BookInfo>,
    val users: List<UserProfile>,
    val totalCount: Int
) {
    companion object {
        fun Empty(query: String) = SearchResults(
            reviews = emptyList(),
            discussions = emptyList(), 
            books = emptyList(),
            users = emptyList(),
            totalCount = 0
        )
    }
}

data class TrendingTopic(
    val name: String,
    val hotScore: Int,
    val hashtag: String
)

data class ReadingChallenge(
    val id: String,
    val title: String,
    val description: String,
    val targetBooks: Int,
    val durationDays: Int,
    val reward: String,
    val difficulty: ChallengeDifficulty,
    val participants: List<String> = emptyList(),
    val creatorId: String = "",
    val createdAt: Long = 0L
)

enum class ChallengeDifficulty {
    EASY, MEDIUM, HARD
}

data class ChallengeResult(
    val success: Boolean,
    val challengeId: String?,
    val error: String?
) {
    companion object {
        fun Success(id: String) = ChallengeResult(true, id, null)
        fun Failure(error: String) = ChallengeResult(false, null, error)
    }
}

data class JoinResult(
    val success: Boolean,
    val challengeProgress: ChallengeProgress?,
    val error: String?
) {
    companion object {
        fun Success(progress: ChallengeProgress) = JoinResult(true, progress, null)
        fun Failure(error: String) = JoinResult(false, null, error)
    }
}

data class ChallengeProgress(
    val challengeId: String,
    val userId: String,
    val currentBooks: Int,
    val completedBooks: List<String>,
    val startDate: Long,
    val isCompleted: Boolean = false
)

data class BookInfo(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val averageRating: Float,
    val reviewCount: Int,
    val tags: List<String>
)

data class UserProfile(
    val id: String,
    val username: String,
    val avatarUrl: String?,
    val bio: String,
    val readingStats: ReadingStats,
    val joinedAt: Long
)

data class ReadingStats(
    val totalBooks: Int,
    val totalPages: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val favoriteGenres: List<String>
)

// API Interface
interface CommunityApi {
    suspend fun getCollaborativeRecommendations(userId: String): List<BookRecommendation>
    suspend fun publishReview(review: CommunityReview)
    suspend fun getBookDiscussion(bookId: String): List<DiscussionThread>
    suspend fun searchCommunityContent(query: String, filters: SearchFilters): SearchResults
    suspend fun createChallenge(challenge: ReadingChallenge): ReadingChallenge
    suspend fun joinChallenge(challengeId: String, userId: String): JoinChallengeResponse
}

// Local Repository Interface  
interface CommunityLocalRepository {
    suspend fun saveReview(review: CommunityReview)
    suspend fun getBookDiscussions(bookId: String): List<DiscussionThread>
    suspend fun searchContent(query: String, filters: SearchFilters): SearchResults
}

// Recommendation Engine
class RecommendationEngine @Inject constructor() {

    suspend fun generateRecommendations(
        userId: String,
        categories: List<String>,
        history: List<String>
    ): List<BookRecommendation> {
        // 基于用户历史和偏好的推荐算法
        return listOf(
            BookRecommendation(
                bookId = "rec_1",
                title = "推荐小说1", 
                author = "作者A",
                coverUrl = null,
                score = 0.9f,
                reason = "基于您的阅读历史",
                source = "local",
                tags = categories
            ),
            BookRecommendation(
                bookId = "rec_2", 
                title = "推荐小说2",
                author = "作者B",
                coverUrl = null,
                score = 0.8f,
                reason = "相似类型推荐",
                source = "local", 
                tags = categories
            )
        )
    }

    suspend fun updateUserPreferences(userId: String, review: CommunityReview) {
        // 根据用户行为更新偏好模型
    }
}

// Response Classes
data class JoinChallengeResponse(
    val success: Boolean,
    val challengeProgress: ChallengeProgress?,
    val errorMessage: String?
)