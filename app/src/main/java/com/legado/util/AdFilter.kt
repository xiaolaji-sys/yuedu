package com.legado.util

import android.util.Log
import java.util.regex.Pattern

/**
 * 智能广告过滤器 - 检测和过滤网页内容中的广告元素
 */
object AdFilter {

    private const val TAG = "AdFilter"

    // 常见广告关键词
    private val adKeywords = listOf(
        "广告", "推广", "赞助", "营销", "促销", "优惠", "折扣",
        "点击", "下载", "注册", "登录", "会员", "VIP",
        "赚钱", "兼职", "副业", "投资", "理财", "股票",
        "彩票", "赌博", "竞猜", "抽奖", "中奖", "奖品",
        "免费", "限时", "抢购", "秒杀", "特价", "清仓",
        "代理", "加盟", "招商", "代理费", "加盟费",
        "贷款", "信用卡", "套现", "还款", "利息",
        "病毒", "木马", "黑客", "破解", "激活码",
        "代购", "转运", "免税店", "平行进口",
        "保健品", "药品", "医疗器械", "减肥药", "壮阳药",
        "贷款", "高利贷", "网贷", "P2P", "众筹",
        "股票", "基金", "期货", "外汇", "比特币",
        "彩票", "六合彩", "赌博", "赌场", "澳门",
        "兼职", "日结", "周结", "月结", "工资",
        "招聘", "找工作", "求职", "面试", "简历"
    )

    // 常见广告链接模式
    private val adPatterns = listOf(
        Pattern.compile(".*(ad|ads|advert|promo|promotion).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.(com|net|org)/.*(ad|ads|promo|promotion|marketing).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*[?&](utm_|ref=|source=).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*banner.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*popup.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*modal.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*overlay.*", Pattern.CASE_INSENSITIVE)
    )

    // HTML广告标签
    private val adHtmlTags = listOf(
        "<iframe", "</iframe>",
        "<script", "</script>",
        "<div[^>]*class=\"[^"]*ad[^"]*\"[^>]*>", // class包含ad的div
        "<div[^>]*id=\"[^"]*ad[^"]*\"[^>]*>",   // id包含ad的div
        "<img[^>]*src=[\"'][^'\"]*ad[^'\"]*[\"']", // img src包含ad的图片
        "javascript:", // javascript链接
        "onclick="     // 点击事件
    )

    /**
     * 检测文本是否包含广告内容
     * @param text 待检测的文本
     * @return 如果检测到广告返回true，否则false
     */
    fun containsAd(text: String): Boolean {
        if (text.isBlank()) return false

        val cleanText = text.trim()

        // 检查广告关键词
        for (keyword in adKeywords) {
            if (cleanText.contains(keyword, ignoreCase = true)) {
                Log.d(TAG, "检测到广告关键词: $keyword")
                return true
            }
        }

        // 检查广告模式
        for (pattern in adPatterns) {
            if (pattern.matcher(cleanText).find()) {
                Log.d(TAG, "检测到广告模式匹配")
                return true
            }
        }

        // 检查HTML广告标签
        for (tag in adHtmlTags) {
            if (cleanText.contains(tag, ignoreCase = true)) {
                Log.d(TAG, "检测到HTML广告标签: $tag")
                return true
            }
        }

        return false
    }

    /**
     * 清理HTML内容中的广告
     * @param htmlContent 原始HTML内容
     * @return 清理后的HTML内容
     */
    fun cleanHtmlContent(htmlContent: String): String {
        if (htmlContent.isBlank()) return htmlContent

        var cleanedContent = htmlContent

        try {
            // 移除iframe标签
            cleanedContent = cleanedContent.replace(Regex("<iframe[^>]*>.*?</iframe>", RegexOption.IGNORE_CASE or RegexOption.DOT_MATCHES_ALL), "")

            // 移除script标签
            cleanedContent = cleanedContent.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE or RegexOption.DOT_MATCHES_ALL), "")

            // 移除包含广告的div标签
            cleanedContent = cleanedContent.replace(Regex("<div[^>]*class=[\"']([^\"'])*ad([^\"'])*[\"'][^>]*>.*?</div>", RegexOption.IGNORE_CASE or RegexOption.DOT_MATCHES_ALL), "")

            // 移除包含ad的id的div
            cleanedContent = cleanedContent.replace(Regex("<div[^>]*id=[\"']([^\"'])*ad([^\"'])*[\"'][^>]*>.*?</div>", RegexOption.IGNORE_CASE or RegexOption.DOT_MATCHES_ALL), "")

            // 移除javascript链接
            cleanedContent = cleanedContent.replace(Regex("javascript:[^'\">]*", RegexOption.IGNORE_CASE), "#")

            // 移除onclick等事件属性
            cleanedContent = cleanedContent.replace(Regex("\\s+on\\w+\\s*=\\s*[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE), "")

            // 移除可疑的URL参数
            cleanedContent = cleanedContent.replace(Regex("[?&](utm_[a-z]+|ref|source|campaign)=[^'\">]*", RegexOption.IGNORE_CASE), "")

        } catch (e: Exception) {
            Log.e(TAG, "清理HTML时发生错误: ${e.message}")
        }

        return cleanedContent
    }

    /**
     * 提取纯文本内容，移除所有HTML标签
     * @param htmlContent 包含HTML的内容
     * @return 纯文本内容
     */
    fun extractPlainText(htmlContent: String): String {
        if (htmlContent.isBlank()) return ""

        return try {
            // 移除HTML标签
            var plainText = htmlContent.replace(Regex("<[^>]*>"), " ")
            // 移除多余的空白字符
            plainText = plainText.replace(Regex("\\s+"), " ")
            plainText.trim()
        } catch (e: Exception) {
            Log.e(TAG, "提取纯文本时发生错误: ${e.message}")
            ""
        }
    }

    /**
     * 检查URL是否为广告链接
     * @param url 要检查的URL
     * @return 如果是广告链接返回true
     */
    fun isAdUrl(url: String): Boolean {
        if (url.isBlank()) return false

        val lowerUrl = url.toLowerCase()

        // 检查广告域名
        val adDomains = listOf(
            "googleadservices.com", "doubleclick.net", "adnetwork.com",
            "adservice.google.com", "googlesyndication.com", "ad.doubleclick.net"
        )

        for (domain in adDomains) {
            if (lowerUrl.contains(domain)) {
                Log.d(TAG, "检测到广告域名: $domain")
                return true
            }
        }

        // 检查广告关键词在URL中
        for (keyword in adKeywords) {
            if (lowerUrl.contains(keyword.toLowerCase())) {
                Log.d(TAG, "检测到广告关键词在URL中: $keyword")
                return true
            }
        }

        return false
    }
}