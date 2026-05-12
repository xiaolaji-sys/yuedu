package com.legado.util

import android.content.Context
import android.util.Log

/**
 * 广告净化管理器 - 提供统一的广告净化接口
 */
class AdPurificationManager(private val context: Context) {

    companion object {
        private const val TAG = "AdPurificationManager"
    }

    /**
     * 净化章节内容
     * @param rawContent 原始章节内容
     * @return 净化后的内容
     */
    fun purifyChapterContent(rawContent: String): String {
        Log.d(TAG, "开始净化章节内容")

        if (rawContent.isBlank()) {
            Log.w(TAG, "内容为空，直接返回")
            return rawContent
        }

        try {
            // 1. 清理文本内容
            var purified = ContentCleaner.cleanText(rawContent)

            // 2. 优化段落格式
            purified = ContentCleaner.optimizeParagraphs(purified)

            // 3. 去除重复内容
            purified = ContentCleaner.removeDuplicates(purified, 0.85f)

            // 4. 智能截断（防止内容过长）
            purified = ContentCleaner.smartTruncate(purified, 50000) // 限制在5万字以内

            Log.d(TAG, "章节内容净化完成，原始长度: ${rawContent.length}, 净化后长度: ${purified.length}")
            return purified

        } catch (e: Exception) {
            Log.e(TAG, "净化章节内容时发生错误: ${e.message}", e)
            return rawContent // 出错时返回原始内容
        }
    }

    /**
     * 净化URL链接
     * @param url 原始URL
     * @return 如果是广告链接返回null，否则返回净化后的URL
     */
    fun purifyUrl(url: String?): String? {
        if (url == null || url.isBlank()) return null

        return if (AdFilter.isAdUrl(url)) {
            Log.d(TAG, "检测到广告链接，已过滤: $url")
            null
        } else {
            // 清理URL中的可疑参数
            val cleanedUrl = url.replace(Regex("[?&](utm_[a-z]+|ref|source|campaign)=[^'\">]*", RegexOption.IGNORE_CASE), "")
            if (cleanedUrl != url) {
                Log.d(TAG, "清理URL可疑参数: $url -> $cleanedUrl")
            }
            cleanedUrl
        }
    }

    /**
     * 批量净化多个章节内容
     * @param contents 章节内容列表
     * @return 净化后的内容列表
     */
    fun purifyChapterContents(contents: List<String>): List<String> {
        return contents.map { purifyChapterContent(it) }
            .filter { it.isNotBlank() }
    }

    /**
     * 检查内容是否包含广告
     * @param content 待检查的内容
     * @return 如果包含广告返回true
     */
    fun containsAdvertisement(content: String): Boolean {
        return AdFilter.containsAd(content)
    }
}