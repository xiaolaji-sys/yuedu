package com.legado.data.source

import com.legado.data.network.model.Book
import com.legado.data.source.rules.RuleEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL

data class BookSource(
    val id: String,
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    val lastUpdate: Long = 0L,
    val description: String? = null,
    val rules: List<Rule> = emptyList()
)

data class Rule(
    val selector: String,
    val attribute: String? = null,
    val regex: String? = null,
    val replacement: String? = null
)

class BookSourceManager(private val ruleEngine: RuleEngine) {

    private val bookSources = mutableMapOf<String, BookSource>()

    suspend fun addBookSource(source: BookSource): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Validate the source URL
                URL(source.baseUrl)
                bookSources[source.id] = source
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun removeBookSource(sourceId: String): Boolean {
        return bookSources.remove(sourceId) != null
    }

    suspend fun updateBookSource(source: BookSource): Boolean {
        return if (bookSources.containsKey(source.id)) {
            bookSources[source.id] = source
            true
        } else {
            false
        }
    }

    fun getBookSource(sourceId: String): BookSource? {
        return bookSources[sourceId]
    }

    fun getAllBookSources(): List<BookSource> {
        return bookSources.values.toList()
    }

    fun getEnabledBookSources(): List<BookSource> {
        return bookSources.values.filter { it.enabled }
    }

    suspend fun searchBooks(query: String, sourceIds: List<String>? = null): List<Book> {
        return withContext(Dispatchers.IO) {
            val sources = if (sourceIds != null) {
                bookSources.values.filter { it.id in sourceIds && it.enabled }
            } else {
                getEnabledBookSources()
            }

            val allBooks = mutableListOf<Book>()

            for (source in sources) {
                try {
                    val books = searchInSource(query, source)
                    allBooks.addAll(books)
                } catch (e: Exception) {
                    // Log error but continue with other sources
                    println("Error searching in source ${source.name}: ${e.message}")
                }
            }

            allBooks
        }
    }

    suspend fun discoverBooks(sourceId: String): List<Book> {
        val source = bookSources[sourceId] ?: return emptyList()
        return withContext(Dispatchers.IO) {
            discoverInSource(source)
        }
    }

    private suspend fun searchInSource(query: String, source: BookSource): List<Book> {
        val document = Jsoup.connect(source.baseUrl + "/search?q=$query")
            .timeout(10000)
            .get()

        val books = mutableListOf<Book>()

        // Apply rules to extract book information
        for (rule in source.rules) {
            val elements = ruleEngine.extractElements(document, rule.selector)
            for (element in elements) {
                val title = ruleEngine.extractText(element, "title") ?: ""
                val author = ruleEngine.extractText(element, "author") ?: ""
                val cover = ruleEngine.extractAttribute(element, "cover", "src")

                if (title.isNotBlank()) {
                    val book = Book(
                        id = "${source.id}_${title.hashCode()}",
                        title = title,
                        author = author,
                        cover = cover,
                        source = source.name,
                        description = ruleEngine.extractText(element, "description"),
                        categories = listOfNotNull(ruleEngine.extractText(element, "category")),
                        rating = ruleEngine.extractNumericValue(element, "rating")?.toDouble(),
                        wordCount = ruleEngine.extractNumericValue(element, "wordCount")?.toLong()
                    )
                    books.add(book)
                }
            }
        }

        return books
    }

    private suspend fun discoverInSource(source: BookSource): List<Book> {
        val document = Jsoup.connect(source.baseUrl)
            .timeout(10000)
            .get()

        val books = mutableListOf<Book>()

        // Apply discovery rules
        for (rule in source.rules) {
            if (rule.selector.contains("popular") || rule.selector.contains("latest")) {
                val elements = ruleEngine.extractElements(document, rule.selector)
                for (element in elements) {
                    val title = ruleEngine.extractText(element, "title") ?: ""
                    val author = ruleEngine.extractText(element, "author") ?: ""

                    if (title.isNotBlank()) {
                        val book = Book(
                            id = "${source.id}_${title.hashCode()}",
                            title = title,
                            author = author,
                            source = source.name,
                            status = ruleEngine.extractText(element, "status"),
                            lastChapter = ruleEngine.extractText(element, "lastChapter"),
                            updateTime = System.currentTimeMillis()
                        )
                        books.add(book)
                    }
                }
            }
        }

        return books
    }

    suspend fun refreshSource(sourceId: String): Boolean {
        val source = bookSources[sourceId] ?: return false
        return try {
            // Update last update time
            val updatedSource = source.copy(lastUpdate = System.currentTimeMillis())
            bookSources[sourceId] = updatedSource
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun enableSource(sourceId: String): Boolean {
        val source = bookSources[sourceId] ?: return false
        bookSources[sourceId] = source.copy(enabled = true)
        return true
    }

    suspend fun disableSource(sourceId: String): Boolean {
        val source = bookSources[sourceId] ?: return false
        bookSources[sourceId] = source.copy(enabled = false)
        return true
    }
}