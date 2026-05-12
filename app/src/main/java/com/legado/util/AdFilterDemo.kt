package com.legado.util

/**
 * 广告净化功能使用示例
 *
 * 使用方法：
 * 1. AdFilter.kt - 用于检测和处理广告内容
 * 2. ContentCleaner.kt - 用于清理和优化文本内容
 *
 * 典型使用场景：
 * - 在获取章节内容后，先使用 cleanText() 清理
 * - 使用 optimizeParagraphs() 优化段落格式
 * - 使用 removeDuplicates() 去除重复内容
 */

fun main() {
    // 示例：清理包含广告的HTML内容
    val dirtyHtml = """
        <div class="ad-banner">这是一个广告</div>
        <p>这是正常的章节内容。</p>
        <script>alert('点击这里赚钱！');</script>
        <div id="promo">限时优惠，立即注册！</div>
        <p>这里是更多正常的内容。</p>
    """.trimIndent()

    println("=== 原始内容 ===")
    println(dirtyHtml)
    println()

    println("=== 清理后的内容 ===")
    val cleaned = ContentCleaner.cleanText(dirtyHtml)
    println(cleaned)
    println()

    println("=== 优化段落 ===")
    val optimized = ContentCleaner.optimizeParagraphs(cleaned)
    println(optimized)
    println()

    // 示例：检测URL是否为广告链接
    val urls = listOf(
        "https://www.example.com/article",
        "http://ads.google.com/click",
        "https://doubleclick.net/promo"
    )

    println("=== URL广告检测 ===")
    for (url in urls) {
        val isAd = AdFilter.isAdUrl(url)
        println("$url -> ${if (isAd) "广告链接" else "正常链接"}")
    }
}