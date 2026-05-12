package com.legado.presentation.screens.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legado.data.network.model.Book
import com.legado.data.source.BookSourceManager
import com.legado.data.source.rules.RuleEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookSourceViewModel(
    private val bookSourceManager: BookSourceManager,
    private val ruleEngine: RuleEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookSourceUiState>(BookSourceUiState.Loading)
    val uiState: StateFlow<BookSourceUiState> = _uiState.asStateFlow()

    private var showDialog = false
    private var editingSource: com.legado.data.source.BookSource? = null

    init {
        loadBookSources()
    }

    fun loadBookSources() {
        viewModelScope.launch {
            _uiState.value = BookSourceUiState.Loading
            try {
                // Initialize with some default sources if none exist
                val sources = bookSourceManager.getAllBookSources()
                if (sources.isEmpty()) {
                    initializeDefaultSources()
                }
                _uiState.value = BookSourceUiState.Success(bookSourceManager.getAllBookSources())
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error("加载书源失败: ${e.message}")
            }
        }
    }

    private suspend fun initializeDefaultSources() {
        val defaultSources = listOf(
            com.legado.data.source.BookSource(
                id = "sample_source_1",
                name = "示例书源1",
                baseUrl = "https://example-book-source.com",
                description = "一个示例书源，用于演示功能",
                rules = listOf(
                    com.legado.data.source.Rule(
                        selector = ".book-item",
                        attribute = "title"
                    ),
                    com.legado.data.source.Rule(
                        selector = ".book-item",
                        attribute = "author"
                    )
                )
            ),
            com.legado.data.source.BookSource(
                id = "sample_source_2",
                name = "示例书源2",
                baseUrl = "https://another-book-site.org",
                description = "另一个示例书源",
                rules = listOf(
                    com.legado.data.source.Rule(
                        selector = ".novel-card",
                        attribute = "title"
                    ),
                    com.legado.data.source.Rule(
                        selector = ".novel-card",
                        attribute = "author"
                    )
                )
            )
        )

        for (source in defaultSources) {
            bookSourceManager.addBookSource(source)
        }
    }

    fun addSource(source: com.legado.data.source.BookSource) {
        viewModelScope.launch {
            try {
                val success = bookSourceManager.addBookSource(source)
                if (success) {
                    loadBookSources()
                } else {
                    _uiState.value = BookSourceUiState.Error("添加书源失败")
                }
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error("添加书源失败: ${e.message}")
            }
        }
    }

    fun updateSource(source: com.legado.data.source.BookSource) {
        viewModelScope.launch {
            try {
                val success = bookSourceManager.updateBookSource(source)
                if (success) {
                    loadBookSources()
                } else {
                    _uiState.value = BookSourceUiState.Error("更新书源失败")
                }
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error("更新书源失败: ${e.message}")
            }
        }
    }

    fun deleteSource(sourceId: String) {
        viewModelScope.launch {
            try {
                val success = bookSourceManager.removeBookSource(sourceId)
                if (success) {
                    loadBookSources()
                } else {
                    _uiState.value = BookSourceUiState.Error("删除书源失败")
                }
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error("删除书源失败: ${e.message}")
            }
        }
    }

    fun toggleSourceEnabled(sourceId: String) {
        viewModelScope.launch {
            try {
                val source = bookSourceManager.getBookSource(sourceId)
                if (source != null) {
                    if (source.enabled) {
                        bookSourceManager.disableSource(sourceId)
                    } else {
                        bookSourceManager.enableSource(sourceId)
                    }
                    loadBookSources()
                }
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error("切换书源状态失败: ${e.message}")
            }
        }
    }

    fun editSource(source: com.legado.data.source.BookSource) {
        editingSource = source
        showDialog = true
        // Update UI state to show dialog
        _uiState.value = BookSourceUiState.Success(bookSourceManager.getAllBookSources())
    }

    fun hideDialog() {
        showDialog = false
        editingSource = null
        _uiState.value = BookSourceUiState.Success(bookSourceManager.getAllBookSources())
    }

    fun refreshSources() {
        viewModelScope.launch {
            try {
                val sources = bookSourceManager.getAllBookSources()
                for (source in sources) {
                    bookSourceManager.refreshSource(source.id)
                }
                loadBookSources()
            } catch (e: Exception) {
                _uiState.value = BookSourceUiState.Error("刷新书源失败: ${e.message}")
            }
        }
    }

    suspend fun searchBooks(query: String, sourceIds: List<String>? = null): List<Book> {
        return bookSourceManager.searchBooks(query, sourceIds)
    }

    suspend fun discoverBooks(sourceId: String): List<Book> {
        return bookSourceManager.discoverBooks(sourceId)
    }
}

// UI State sealed class
sealed class BookSourceUiState {
    object Loading : BookSourceUiState()
    data class Success(val bookSources: List<com.legado.data.source.BookSource>) : BookSourceUiState()
    data class Error(val message: String) : BookSourceUiState()

    // Dialog state
    var showDialog: Boolean = false
        private set
    var editingSource: com.legado.data.source.BookSource? = null
        private set

    fun showAddDialog() {
        showDialog = true
        editingSource = null
    }

    fun showEditDialog(source: com.legado.data.source.BookSource) {
        showDialog = true
        editingSource = source
    }

    fun hideDialog() {
        showDialog = false
        editingSource = null
    }
}