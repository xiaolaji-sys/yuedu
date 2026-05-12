package com.legado.data.source.rules

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.regex.Pattern

class RuleEngine {

    fun extractElements(document: Document, selector: String): Elements {
        return try {
            document.select(selector)
        } catch (e: Exception) {
            // Return empty elements if selector is invalid
            org.jsoup.select.Elements()
        }
    }

    fun extractText(element: Element, attribute: String): String? {
        return when (attribute.lowercase()) {
            "title" -> element.select("h1, h2, h3, .title, [class*=title]").first()?.text()?.trim()
            "author" -> element.select(".author, [class*=author], .writer").first()?.text()?.trim()
            "description" -> element.select(".desc, [class*=desc], .summary, p").first()?.text()?.trim()
            "category" -> element.select(".cat, [class*=cat], .category, span").first()?.text()?.trim()
            "status" -> element.select(".status, [class*=status]").first()?.text()?.trim()
            "lastchapter" -> element.select(".last-chapter, [class*=chapter], .chapter").first()?.text()?.trim()
            else -> null
        }
    }

    fun extractAttribute(element: Element, attribute: String, attrName: String): String? {
        return when (attribute.lowercase()) {
            "cover" -> element.select("img[src]").attr("src")
            else -> element.attr(attrName)
        }
    }

    fun extractNumericValue(element: Element, field: String): Long? {
        val text = extractText(element, field) ?: return null
        return text.filter { it.isDigit() }.toLongOrNull()
    }

    fun applyRules(text: String, rules: List<Rule>): String {
        var result = text

        for (rule in rules) {
            if (rule.regex != null) {
                val pattern = Pattern.compile(rule.regex)
                val matcher = pattern.matcher(result)
                result = matcher.replaceAll(rule.replacement ?: "")
            }
        }

        return result
    }

    fun cleanHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").replace(Regex("\\s+"), " ").trim()
    }

    fun normalizeUrl(baseUrl: String, relativeUrl: String): String {
        return try {
            val base = java.net.URL(baseUrl)
            val normalizedBase = "${base.protocol}://${base.host}${if (base.port != -1) ":${base.port}" else ""}"
            if (relativeUrl.startsWith("/")) {
                "$normalizedBase$relativeUrl"
            } else {
                val path = base.path.substringBeforeLast("/")
                "$normalizedBase$path/$relativeUrl"
            }
        } catch (e: Exception) {
            relativeUrl
        }
    }

    fun extractBookInfo(element: Element, sourceRules: List<Rule>): BookInfo {
        val title = extractText(element, "title") ?: ""
        val author = extractText(element, "author") ?: ""
        val description = extractText(element, "description") ?: ""
        val cover = extractAttribute(element, "cover", "src")
        val category = extractText(element, "category")

        // Apply custom rules for this element
        var processedTitle = title
        var processedAuthor = author
        var processedDescription = description

        for (rule in sourceRules) {
            if (rule.regex != null && rule.replacement != null) {
                processedTitle = applyRules(processedTitle, listOf(rule))
                processedAuthor = applyRules(processedAuthor, listOf(rule))
                processedDescription = applyRules(processedDescription, listOf(rule))
            }
        }

        return BookInfo(
            title = processedTitle,
            author = processedAuthor,
            description = processedDescription,
            cover = cover,
            category = category
        )
    }

    data class BookInfo(
        val title: String,
        val author: String,
        val description: String,
        val cover: String?,
        val category: String?
    )

    data class Rule(
        val selector: String,
        val attribute: String? = null,
        val regex: String? = null,
        val replacement: String? = null
    )
}

// Extension function to make it easier to work with rules
fun List<Rule>.applyToElement(element: Element): RuleEngine.BookInfo {
    val engine = RuleEngine()
    return engine.extractBookInfo(element, this)
}