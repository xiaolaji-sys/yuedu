package com.legado.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legado.data.database.entities.BookEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val books: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val viewMode: ViewMode = ViewMode.GRID,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate loading delay and mock data
            delay(1000)

            val mockBooks = generateMockBooks()
            _uiState.update {
                it.copy(
                    books = mockBooks,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun toggleViewMode() {
        _uiState.update { currentState ->
            when (currentState.viewMode) {
                ViewMode.GRID -> currentState.copy(viewMode = ViewMode.LIST)
                ViewMode.LIST -> currentState.copy(viewMode = ViewMode.GRID)
            }
        }
    }

    fun refreshBooks() {
        loadBooks()
    }

    private fun generateMockBooks(): List<BookEntity> {
        return listOf(
            BookEntity(
                bookId = "1",
                name = "三体",
                author = "刘慈欣",
                coverUrl = null,
                intro = "地球文明向宇宙发出的一声啼鸣，最后被更高级的文明毁灭",
                category = "科幻",
                lastCheckTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis(),
                createTime = System.currentTimeMillis(),
                wordCount = 800000,
                chaptersCount = 30,
                toChapterPosition = 25,
                currentChapterPosition = 25,
                sourceId = "source1",
                originName = "三体",
                customCover = null
            ),
            BookEntity(
                bookId = "2",
                name = "活着",
                author = "余华",
                coverUrl = null,
                intro = "一个人和他命运之间的友情，这是最为感人的友情",
                category = "文学",
                lastCheckTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis(),
                createTime = System.currentTimeMillis(),
                wordCount = 600000,
                chaptersCount = 20,
                toChapterPosition = 15,
                currentChapterPosition = 10,
                sourceId = "source1",
                originName = "活着",
                customCover = null
            ),
            BookEntity(
                bookId = "3",
                name = "百年孤独",
                author = "加西亚·马尔克斯",
                coverUrl = null,
                intro = "魔幻现实主义的代表作，布恩迪亚家族七代人的传奇故事",
                category = "文学",
                lastCheckTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis(),
                createTime = System.currentTimeMillis(),
                wordCount = 900000,
                chaptersCount = 25,
                toChapterPosition = 25,
                currentChapterPosition = 25,
                sourceId = "source1",
                originName = "百年孤独",
                customCover = null
            ),
            BookEntity(
                bookId = "4",
                name = "小王子",
                author = "圣埃克苏佩里",
                coverUrl = null,
                intro = "来自外星球的小王子的故事，关于爱与责任",
                category = "童话",
                lastCheckTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis(),
                createTime = System.currentTimeMillis(),
                wordCount = 30000,
                chaptersCount = 27,
                toChapterPosition = 20,
                currentChapterPosition = 18,
                sourceId = "source1",
                originName = "小王子",
                customCover = null
            ),
            BookEntity(
                bookId = "5",
                name = "白夜行",
                author = "东野圭吾",
                coverUrl = null,
                intro = "一宗离奇命案牵出跨度近20年步步惊心的故事",
                category = "推理",
                lastCheckTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis(),
                createTime = System.currentTimeMillis(),
                wordCount = 700000,
                chaptersCount = 28,
                toChapterPosition = 28,
                currentChapterPosition = 28,
                sourceId = "source1",
                originName = "白夜行",
                customCover = null
            )
        )
    }
}