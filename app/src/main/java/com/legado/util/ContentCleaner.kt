package com.legado.util

import android.util.Log
import java.util.regex.Pattern

/**
 * 内容清理器 - 清理和优化文本内容
 */
object ContentCleaner {

    private const val TAG = "ContentCleaner"

    // 常见噪音模式
    private val noisePatterns = listOf(
        // 重复的空行
        Pattern.compile("\\n\\s*\\n\\s*\\n+"),
        // 过多的空白字符
        Pattern.compile("[ \\t]{2,}"),
        // 孤立的标点符号
        Pattern.compile("^[。！？!?]+$", Pattern.MULTILINE),
        // 孤立的字母数字
        Pattern.compile("^[a-zA-Z0-9]{1,3}$", Pattern.MULTILINE),
        // 过多的连字符
        Pattern.compile("-{3,}")
    )

    // 需要保留的重要标点符号
    private val importantPunctuation = setOf("，", "。", "！", "？", "：", "；", "、", "（", "）", "《", "》", "【", "】", "“", "”")

    /**
     * 清理文本内容，移除噪音和格式化内容
     * @param text 原始文本
     * @return 清理后的文本
     */
    fun cleanText(text: String): String {
        if (text.isBlank()) return ""

        var cleaned = text

        try {
            // 移除广告内容
            cleaned = AdFilter.cleanHtmlContent(cleaned)

            // 提取纯文本
            cleaned = AdFilter.extractPlainText(cleaned)

            // 移除噪音模式
            for (pattern in noisePatterns) {
                cleaned = pattern.matcher(cleaned).replaceAll { matchResult ->
                    when {
                        // 处理重复空行
                        matchResult.group().matches("^\\s*$".toRegex()) -> "\n"
                        // 处理多余空白字符
                        matchResult.group().length > 4 -> " "
                        // 处理孤立的标点符号
                        else -> ""
                    }
                }
            }

            // 规范化空格和换行符
            cleaned = cleaned.replace(Regex("\\s+"), " ")
            cleaned = cleaned.trim()

            // 确保段落之间有合适的换行
            cleaned = cleaned.replace(Regex("([。！？!?])\\s*([a-zA-Z])"), "$1\n\n$2")

            // 移除孤立的标点符号行
            cleaned = cleaned.replace(Regex("^[。！？!?]+$\\n?", Pattern.MULTILINE), "")

            // 移除孤立的字母数字行
            cleaned = cleaned.replace(Regex("^[a-zA-Z0-9]{1,3}$\\n?", Pattern.MULTILINE), "")

        } catch (e: Exception) {
            Log.e(TAG, "清理文本时发生错误: ${e.message}")
        }

        return cleaned
    }

    /**
     * 优化段落格式
     * @param text 原始文本
     * @return 优化后的文本
     */
    fun optimizeParagraphs(text: String): String {
        if (text.isBlank()) return ""

        var optimized = text

        try {
            // 分割成句子
            val sentences = optimized.split(Regex("[。！？!?]"))
            val resultBuilder = StringBuilder()

            for (sentence in sentences) {
                val trimmed = sentence.trim()
                if (trimmed.isNotEmpty()) {
                    // 检查是否是重要句子（包含中文字符或重要词汇）
                    if (isImportantSentence(trimmed)) {
                        resultBuilder.append(trimmed)
                        // 添加适当的标点符号
                        if (!trimmed.endsWith("。") && !trimmed.endsWith("！") && !trimmed.endsWith("？")) {
                            resultBuilder.append("。")
                        }
                        resultBuilder.append("\n\n")
                    }
                }
            }

            optimized = resultBuilder.toString().trim()

        } catch (e: Exception) {
            Log.e(TAG, "优化段落时发生错误: ${e.message}")
        }

        return optimized
    }

    /**
     * 判断句子是否重要（不是噪音）
     * @param sentence 待判断的句子
     * @return 如果是重要句子返回true
     */
    private fun isImportantSentence(sentence: String): Boolean {
        // 如果句子包含中文字符，通常是重要的
        if (sentence.any { it.isLetter() && it.code > 127 }) {
            return true
        }

        // 检查是否包含重要标点符号
        if (importantPunctuation.any { sentence.contains(it) }) {
            return true
        }

        // 检查长度（太短的可能是噪音）
        if (sentence.length < 5) {
            return false
        }

        // 检查是否包含常见的重要词汇
        val importantWords = listOf("是", "有", "在", "为", "对", "从", "到", "和", "与", "但", "而", "已", "将", "要", "可以", "可能", "应该", "必须", "需要", "能够")
        return importantWords.any { sentence.contains(it) }
    }

    /**
     * 移除重复内容
     * @param text 原始文本
     * @param maxSimilarity 最大相似度阈值（0-1）
     * @return 去重后的文本
     */
    fun removeDuplicates(text: String, maxSimilarity: Float = 0.8f): String {
        if (text.isBlank()) return ""

        val lines = text.lines().filter { it.trim().isNotEmpty() }
        val uniqueLines = mutableListOf<String>()
        val processedLines = mutableSetOf<String>()

        for (line in lines) {
            val normalizedLine = normalizeForComparison(line.trim())
            var isDuplicate = false

            for (processedLine in processedLines) {
                if (calculateSimilarity(normalizedLine, processedLine) > maxSimilarity) {
                    isDuplicate = true
                    break
                }
            }

            if (!isDuplicate) {
                uniqueLines.add(line.trim())
                processedLines.add(normalizedLine)
            }
        }

        return uniqueLines.joinToString("\n\n")
    }

    /**
     * 标准化文本用于比较
     * @param text 原始文本
     * @return 标准化后的文本
     */
    private fun normalizeForComparison(text: String): String {
        return text
            .replace(Regex("\\s+"), "")
            .replace(Regex("[。！？!?，；：、]"), "")
            .toLowerCase()
    }

    /**
     * 计算两个字符串的相似度
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 相似度（0-1）
     */
    private fun calculateSimilarity(str1: String, str2: String): Float {
        if (str1 == str2) return 1.0f
        if (str1.isEmpty() || str2.isEmpty()) return 0.0f

        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1

        val distance = levenshteinDistance(longer, shorter)
        return (longer.length - distance).toFloat() / longer.length.toFloat()
    }

    /**
     * 计算Levenshtein距离
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 编辑距离
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val matrix = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            matrix[i][0] = i
        }

        for (j in 0..str2.length) {
            matrix[0][j] = j
        }

        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                matrix[i][j] = minOf(
                    matrix[i - 1][j] + 1,
                    matrix[i][j - 1] + 1,
                    matrix[i - 1][j - 1] + cost
                )
            }
        }

        return matrix[str1.length][str2.length]
    }

    /**
     * 格式化章节标题
     * @param title 原始标题
     * @return 格式化后的标题
     */
    fun formatChapterTitle(title: String): String {
        if (title.isBlank()) return ""

        var formatted = title.trim()

        // 移除多余的空白字符
        formatted = formatted.replace(Regex("\\s+"), " ")

        // 确保以适当的标点符号结尾
        if (formatted.isNotEmpty() &&
            !formatted.endsWith("。") &&
            !formatted.endsWith("！") &&
            !formatted.endsWith("？") &&
            !formatted.endsWith(":") &&
            !formatted.endsWith("：")) {
            formatted += "："
        }

        return formatted
    }

    /**
     * 智能截断文本，保持完整性
     * @param text 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    fun smartTruncate(text: String, maxLength: Int = 1000): String {
        if (text.length <= maxLength) return text

        var truncated = text.substring(0, maxLength)

        // 找到最后一个完整的句子作为截断点
        val lastPeriod = truncated.lastIndexOf("。", maxLength)
        val lastExclamation = truncated.lastIndexOf("！", maxLength)
        val lastQuestion = truncated.lastIndexOf("？", maxLength)

        val lastPunctuation = listOf(lastPeriod, lastExclamation, lastQuestion)
            .filter { it >= 0 }
            .maxOrNull()

        if (lastPunctuation != null && lastPunctuation > maxLength * 0.7) {
            truncated = truncated.substring(0, lastPunctuation + 1)
        }

        return truncated.trim()
    }
}