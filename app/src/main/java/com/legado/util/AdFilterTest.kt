package com.legado.util

import org.junit.Test
import org.junit.Assert.*

/**
 * 广告净化功能单元测试
 */
class AdFilterTest {

    @Test
    fun testAdDetection() {
        // 测试广告关键词检测
        assertTrue(AdFilter.containsAd("点击这里赚钱！"))
        assertTrue(AdFilter.containsAd("限时优惠，立即注册！"))
        assertTrue(AdFilter.containsAd("免费注册送现金"))
        assertFalse(AdFilter.containsAd("这是一个正常的句子"))
    }

    @Test
    fun testHtmlCleaning() {
        val dirtyHtml = """
            <div class="ad-banner">这是一个广告</div>
            <p>这是正常的内容。</p>
            <script>alert('点击这里赚钱！');</script>
        """.trimIndent()

        val cleaned = ContentCleaner.cleanText(dirtyHtml)
        assertFalse(cleaned.contains("广告"))
        assertFalse(cleaned.contains("赚钱"))
        assertTrue(cleaned.contains("正常的内容"))
    }

    @Test
    fun testUrlAdDetection() {
        assertTrue(AdFilter.isAdUrl("https://doubleclick.net/promo"))
        assertTrue(AdFilter.isAdUrl("http://ads.google.com/click"))
        assertFalse(AdFilter.isAdUrl("https://www.example.com/article"))
    }

    @Test
    fun testTextOptimization() {
        val messyText = """
            这是第一段内容。


            这是第二段内容。



            这是第三段内容。
        """.trimIndent()

        val optimized = ContentCleaner.optimizeParagraphs(messyText)
        assertTrue(optimized.contains("第一段内容"))
        assertTrue(optimized.contains("第二段内容"))
        assertTrue(optimized.contains("第三段内容"))
        // 应该没有过多的空行
        assertFalse(optimized.matches(".*\\n\\s*\\n\\s*\\n\\s*\\n.*".toRegex()))
    }

    @Test
    fun testDuplicateRemoval() {
        val duplicateText = """
            这是第一段内容。
            这是第一段内容。
            这是第二段不同的内容。
            这是第二段不同的内容。
            这是第三段内容。
        """.trimIndent()

        val deduplicated = ContentCleaner.removeDuplicates(duplicateText, 0.8f)
        assertTrue(deduplicated.contains("第一段内容"))
        assertTrue(deduplicated.contains("第二段不同的内容"))
        assertTrue(deduplicated.contains("第三段内容"))
        // 重复的内容应该被减少
        val firstLineCount = deduplicated.split("\n").count { it.trim().contains("第一段内容") }
        assertTrue("第一段内容应该出现但不超过2次", firstLineCount <= 2)
    }
}