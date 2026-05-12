package com.legado.ai

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI智能目录生成器 - 使用大语言模型分析文本并生成章节结构
 */
@Singleton
class DirectoryGenerator @Inject constructor(
    private val context: Context,
    private val llmService: LLMService,
    private val textAnalyzer: TextAnalyzer
) {

    /**
     * 从小说内容生成智能目录
     */
    suspend fun generateTableOfContents(content: String, bookTitle: String = ""): AITableOfContents? {
        return try {
            // 1. 文本预处理和分析
            val analysisResult = textAnalyzer.analyzeContent(content)
            
            // 2. 构建AI提示词
            val prompt = buildDirectoryPrompt(content, bookTitle, analysisResult)
            
            // 3. 调用LLM服务
            val aiResponse = llmService.generateCompletion(prompt, "directory-generation")
            
            // 4. 解析AI响应
            parseAIResponse(aiResponse, analysisResult)
            
        } catch (e: Exception) {
            println("AI目录生成失败: ${e.message}")
            null
        }
    }

    /**
     * 增量更新目录 - 仅对新增内容进行分析
     */
    suspend fun updateTableOfContents(
        existingToc: AITableOfContents,
        newContent: String,
        startIndex: Int
    ): AITableOfContents {
        return try {
            // 1. 分析新增内容
            val newAnalysis = textAnalyzer.analyzeContent(newContent)
            
            // 2. 构建增量更新提示词
            val prompt = buildIncrementalUpdatePrompt(existingToc, newContent, newAnalysis)
            
            // 3. 调用LLM进行增量更新
            val aiResponse = llmService.generateCompletion(prompt, "incremental-update")
            
            // 4. 合并新旧目录
            mergeDirectoryUpdates(existingToc, aiResponse, startIndex)
            
        } catch (e: Exception) {
            println("增量目录更新失败: ${e.message}")
            existingToc // 返回原有目录作为降级方案
        }
    }

    /**
     * 智能章节分割 - 基于内容特征自动划分章节
     */
    fun autoSplitChapters(content: String): List<ChapterSegment> {
        val segments = mutableListOf<ChapterSegment>()
        
        // 1. 基于标题标记分割
        val titleMarkers = listOf("#", "##", "第.*章", "Chapter\\s+\\d+")
        val titleRegex = Regex(titleMarkers.joinToString("|"))
        
        var lastEndIndex = 0
        titleRegex.findAll(content).forEach { match ->
            if (match.range.start > lastEndIndex + 100) { // 避免过小的章节
                segments.add(ChapterSegment(
                    startPosition = lastEndIndex,
                    endPosition = match.range.start,
                    title = extractChapterTitle(content.substring(lastEndIndex, match.range.start))
                ))
            }
            lastEndIndex = match.range.endInclusive
        }
        
        // 2. 处理最后一段
        if (lastEndIndex < content.length) {
            segments.add(ChapterSegment(
                startPosition = lastEndIndex,
                endPosition = content.length,
                title = "后续内容"
            ))
        }
        
        return segments
    }

    private fun buildDirectoryPrompt(
        content: String,
        bookTitle: String,
        analysis: ContentAnalysis
    ): String {
        val maxLength = minOf(content.length, 5000) // 限制输入长度
        val sampleContent = content.take(maxLength)
        
        return """
            你是一个专业的小说编辑和目录规划师。请基于以下小说内容，分析并生成详细的章节目录结构。

            书籍信息:
            书名: ${bookTitle.ifEmpty { "未知" }}
            总字数: ${analysis.wordCount}
            段落数: ${analysis.paragraphCount}

            内容预览:
            $sampleContent

            请按照以下格式返回JSON:
            {
                "chapters": [
                    {
                        "title": "第一章 标题",
                        "startPosition": 0,
                        "endPosition": 1500,
                        "estimatedWords": 800,
                        "confidence": 0.95,
                        "keyEvents": ["事件1", "事件2"],
                        "suggestedTags": ["玄幻", "战斗"]
                    },
                    ...
                ],
                "summary": "整体情节概述",
                "genre": "小说类型",
                "readingDifficulty": "easy|medium|hard"
            }

            要求:
            1. 分析内容结构和情节发展
            2. 识别关键事件和重要转折点  
            3. 估算每个章节的字数
            4. 提供置信度评分
            5. 建议相关标签
        """.trimIndent()
    }

    private fun buildIncrementalUpdatePrompt(
        existingToc: AITableOfContents,
        newContent: String,
        analysis: ContentAnalysis
    ): String {
        return """
            现有目录:
            ${existingToc.toJsonString()}
            
            新增内容预览:
            ${newContent.take(2000)}
            
            请分析新增内容，生成相应的章节条目，并与现有目录合并。
            只返回新的章节列表，不要重复已有章节。
        """.trimIndent()
    }

    private fun parseAIResponse(response: String, analysis: ContentAnalysis): AITableOfContents {
        return try {
            // 尝试解析JSON响应
            val jsonResponse = response.trim()
            val parsed = Json.decodeFromString<AIDirectoryResponse>(jsonResponse)
            
            AITableOfContents(
                chapters = parsed.chapters.mapIndexed { index, chapter ->
                    ChapterInfo(
                        id = index.toString(),
                        title = chapter.title,
                        startPosition = chapter.startPosition,
                        endPosition = chapter.endPosition,
                        estimatedWords = chapter.estimatedWords,
                        confidence = chapter.confidence,
                        keyEvents = chapter.keyEvents,
                        suggestedTags = chapter.suggestedTags
                    )
                },
                summary = parsed.summary,
                genre = parsed.genre,
                readingDifficulty = parsed.readingDifficulty,
                generatedAt = System.currentTimeMillis(),
                totalChapters = parsed.chapters.size
            )
        } catch (e: Exception) {
            // JSON解析失败时的降级方案
            createFallbackDirectory(analysis)
        }
    }

    private fun createFallbackDirectory(analysis: ContentAnalysis): AITableOfContents {
        val segmentSize = analysis.wordCount / 10 // 平均分成10个章节
        val chapters = (0 until 10).map { index ->
            ChapterInfo(
                id = index.toString(),
                title = "第${index + 1}章",
                startPosition = index * segmentSize,
                endPosition = minOf((index + 1) * segmentSize, analysis.wordCount),
                estimatedWords = segmentSize,
                confidence = 0.5f,
                keyEvents = emptyList(),
                suggestedTags = listOf("默认")
            )
        }

        return AITableOfContents(
            chapters = chapters,
            summary = "自动生成目录",
            genre = "未知类型",
            readingDifficulty = "medium",
            generatedAt = System.currentTimeMillis(),
            totalChapters = chapters.size
        )
    }

    private fun mergeDirectoryUpdates(
        existing: AITableOfContents,
        newChaptersJson: String,
        startIndex: Int
    ): AITableOfContents {
        return try {
            val newChapters = Json.decodeFromString<List<AINewChapter>>(newChaptersJson)
            val updatedChapters = existing.chapters.toMutableList()
            
            // 将新章节添加到指定位置
            newChapters.forEachIndexed { index, chapter ->
                updatedChapters.add(startIndex + index, ChapterInfo(
                    id = "${System.currentTimeMillis()}_$index",
                    title = chapter.title,
                    startPosition = chapter.startPosition,
                    endPosition = chapter.endPosition,
                    estimatedWords = chapter.estimatedWords,
                    confidence = chapter.confidence,
                    keyEvents = chapter.keyEvents,
                    suggestedTags = chapter.suggestedTags
                ))
            }
            
            existing.copy(chapters = updatedChapters)
        } catch (e: Exception) {
            existing // 降级到原有目录
        }
    }

    private fun extractChapterTitle(text: String): String {
        val lines = text.lines().filter { it.trim().isNotEmpty() }
        return if (lines.isNotEmpty()) {
            lines.first().take(50) // 截取前50字符作为标题
        } else {
            "未命名章节"
        }
    }
}

/**
 * 文本分析器 - 分析小说内容的特征和结构
 */
class TextAnalyzer @Inject constructor() {

    data class ContentAnalysis(
        val wordCount: Int,
        val paragraphCount: Int,
        val averageParagraphLength: Float,
        val hasTitleMarkers: Boolean,
        val estimatedReadingTimeMinutes: Int,
        val complexityScore: Float
    )

    fun analyzeContent(content: String): ContentAnalysis {
        val words = content.split("\\s+".toRegex()).count { it.isNotBlank() }
        val paragraphs = content.split("\\n\\s*\\n".toRegex()).filter { it.trim().isNotEmpty() }
        val avgParagraphLength = if (paragraphs.isNotEmpty()) {
            paragraphs.sumOf { it.length } / paragraphs.size.toFloat()
        } else 0f
        
        val hasTitleMarkers = Regex("#+|第.*章|Chapter").containsMatchIn(content)
        val readingTime = (words / 200f).toInt() // 假设每分钟阅读200字
        val complexity = calculateComplexity(content)
        
        return ContentAnalysis(
            wordCount = words,
            paragraphCount = paragraphs.size,
            averageParagraphLength = avgParagraphLength,
            hasTitleMarkers = hasTitleMarkers,
            estimatedReadingTimeMinutes = readingTime,
            complexityScore = complexity
        )
    }

    private fun calculateComplexity(text: String): Float {
        // 简化的复杂度计算
        val sentences = text.split("[。！？.]".toRegex())
        val avgWordsPerSentence = sentences.filter { it.isNotBlank() }.sumOf { 
            it.split("\\s+".toRegex()).size 
        } / maxOf(sentences.size, 1)
        
        return when {
            avgWordsPerSentence < 10 -> 0.3f
            avgWordsPerSentence < 20 -> 0.6f  
            avgWordsPerSentence < 30 -> 0.8f
            else -> 1.0f
        }
    }
}

// Data Classes
data class AITableOfContents(
    val chapters: List<ChapterInfo>,
    val summary: String,
    val genre: String,
    val readingDifficulty: String,
    val generatedAt: Long,
    val totalChapters: Int
) {
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}

data class ChapterInfo(
    val id: String,
    val title: String,
    val startPosition: Int,
    val endPosition: Int,
    val estimatedWords: Int,
    val confidence: Float,
    val keyEvents: List<String>,
    val suggestedTags: List<String>
)

data class ChapterSegment(
    val startPosition: Int,
    val endPosition: Int,
    val title: String
)

// LLM Service Interface
interface LLMService {
    suspend fun generateCompletion(prompt: String, taskType: String): String
}

// JSON Models for AI Response Parsing
data class AIDirectoryResponse(
    val chapters: List<AINewChapter>,
    val summary: String,
    val genre: String,
    val readingDifficulty: String
)

data class AINewChapter(
    val title: String,
    val startPosition: Int,
    val endPosition: Int,
    val estimatedWords: Int,
    val confidence: Float,
    val keyEvents: List<String>,
    val suggestedTags: List<String>
)