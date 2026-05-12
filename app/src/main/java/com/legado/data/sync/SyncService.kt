package com.legado.data.sync

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val context: Context,
    private val apiService: CloudSyncApi,
    private val localRepository: LocalRepository
) {
    suspend fun syncReadingProgress(userId: String) {
        try {
            // 上传本地进度到云端
            val progressList = localRepository.getAllReadingProgress()
            progressList.forEach { progress ->
                apiService.uploadProgress(SyncProgress(
                    userId = userId,
                    bookId = progress.bookId,
                    currentChapter = progress.currentChapter,
                    progressPercentage = progress.progress,
                    timestamp = System.currentTimeMillis()
                ))
            }

            // 下载云端最新进度
            val cloudProgress = apiService.downloadProgress(userId)
            cloudProgress.forEach { progress ->
                localRepository.updateProgress(SyncProgress(
                    userId = progress.userId,
                    bookId = progress.bookId,
                    currentChapter = progress.currentChapter,
                    progressPercentage = progress.progress,
                    timestamp = progress.timestamp
                ))
            }
        } catch (e: Exception) {
            // 记录同步失败，下次重试
            println("同步失败: ${e.message}")
        }
    }

    suspend fun syncBookmarks(userId: String) {
        try {
            // 上传书签
            val localBookmarks = localRepository.getAllBookmarks()
            localBookmarks.forEach { bookmark ->
                apiService.uploadBookmark(SyncBookmark(
                    userId = userId,
                    bookId = bookmark.bookId,
                    chapterIndex = bookmark.chapterIndex,
                    position = bookmark.position,
                    note = bookmark.note,
                    timestamp = System.currentTimeMillis()
                ))
            }

            // 下载云端书签
            val cloudBookmarks = apiService.downloadBookmarks(userId)
            cloudBookmarks.forEach { bookmark ->
                localRepository.addBookmark(SyncBookmark(
                    userId = bookmark.userId,
                    bookId = bookmark.bookId,
                    chapterIndex = bookmark.chapterIndex,
                    position = bookmark.position,
                    note = bookmark.note,
                    timestamp = bookmark.timestamp
                ))
            }
        } catch (e: Exception) {
            println("书签同步失败: ${e.message}")
        }
    }
}

data class SyncProgress(
    val userId: String,
    val bookId: String,
    val currentChapter: Int,
    val progressPercentage: Float,
    val timestamp: Long
)

data class SyncBookmark(
    val userId: String,
    val bookId: String,
    val chapterIndex: Int,
    val position: Int,
    val note: String,
    val timestamp: Long
)

interface CloudSyncApi {
    suspend fun uploadProgress(progress: SyncProgress)
    suspend fun downloadProgress(userId: String): List<SyncProgress>
    suspend fun uploadBookmark(bookmark: SyncBookmark)
    suspend fun downloadBookmarks(userId: String): List<SyncBookmark>
}

interface LocalRepository {
    suspend fun getAllReadingProgress(): List<LocalProgress>
    suspend fun updateProgress(progress: SyncProgress)
    suspend fun getAllBookmarks(): List<LocalBookmark>
    suspend fun addBookmark(bookmark: SyncBookmark)
}

data class LocalProgress(
    val bookId: String,
    val currentChapter: Int,
    val progressPercentage: Float
)

data class LocalBookmark(
    val bookId: String,
    val chapterIndex: Int,
    val position: Int,
    val note: String
)