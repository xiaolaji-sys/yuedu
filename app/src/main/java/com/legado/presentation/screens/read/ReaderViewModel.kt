package com.legado.presentation.screens.read

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.input.InputModifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import com.legado.presentation.screens.read.TTSEnabled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val ttsService: TTSService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(ReaderSettings())
    val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

    // TTS related state
    private val _ttsState = MutableStateFlow(TTSEnabled(false))
    val ttsState: StateFlow<TTSEnabled> = _ttsState.asStateFlow()

    private val _currentChapterContent = MutableStateFlow<String>("")
    val currentChapterContent: StateFlow<String> = _currentChapterContent.asStateFlow()

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Handle scroll events for controls visibility
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            return Velocity.Zero
        }
    }

    fun loadChapter(bookId: Long, chapterPosition: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Simulate loading delay
                delay(500)

                // Mock data - replace with actual repository call
                val mockChapters = generateMockChapters()
                val currentChapter = mockChapters.getOrNull(chapterPosition - 1)
                val chaptersList = mockChapters.map { it.content }

                // Update TTS service with chapter content
                ttsService.setChapters(chaptersList)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentChapter = currentChapter,
                    currentChapterPosition = chapterPosition,
                    totalChapters = mockChapters.size,
                    chapters = mockChapters.map { it.title },
                    showControls = true
                )

                // Update current chapter content for TTS
                currentChapter?.let { chapter ->
                    _currentChapterContent.value = chapter.content
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    // TTS Control Methods

    /**
     * Start TTS reading from current chapter
     */
    fun startTTSReading() {
        val currentPosition = _uiState.value.currentChapterPosition - 1
        ttsService.startReading(currentPosition)
        _ttsState.value = TTSEnabled(true)
    }

    /**
     * Pause TTS reading
     */
    fun pauseTTSReading() {
        ttsService.pauseReading()
        _ttsState.value = TTSEnabled(false, isPaused = true)
    }

    /**
     * Resume TTS reading
     */
    fun resumeTTSReading() {
        ttsService.resumeReading()
        _ttsState.value = TTSEnabled(true)
    }

    /**
     * Stop TTS reading
     */
    fun stopTTSReading() {
        ttsService.stopSpeaking()
        _ttsState.value = TTSEnabled(false)
    }

    /**
     * Toggle TTS on/off
     */
    fun toggleTTS() {
        val currentState = _ttsState.value
        when {
            !currentState.isEnabled -> {
                startTTSReading()
            }
            currentState.isPaused -> {
                resumeTTSReading()
            }
            else -> {
                pauseTTSReading()
            }
        }
    }

    /**
     * Set TTS volume
     */
    fun setTTSSettings(volume: Float? = null, speechRate: Float? = null, pitch: Float? = null) {
        volume?.let { ttsService.setVolume(it) }
        speechRate?.let { ttsService.setSpeechRate(it) }
        pitch?.let { ttsService.setPitch(it) }
    }

    fun goToChapter(position: Float) {
        val chapterPosition = position.roundToInt().coerceIn(1, _uiState.value.totalChapters)
        if (chapterPosition != _uiState.value.currentChapterPosition) {
            loadChapter(chapterPosition)
        }
    }

    fun updateSettings(newSettings: ReaderSettings) {
        _settings.value = newSettings

        // Update UI state with current theme colors
        val currentSettings = _settings.value
        val backgroundColor = when (currentSettings.theme) {
            ReaderTheme.LIGHT -> Color.White
            ReaderTheme.DARK -> Color.Black
            ReaderTheme.SEPIA -> Color(0xFFF4ECD8)
            ReaderTheme.NIGHT -> Color(0xFF1A1A1A)
        }

        val textColor = when (currentSettings.theme) {
            ReaderTheme.LIGHT -> Color.Black
            ReaderTheme.DARK -> Color.White
            ReaderTheme.SEPIA -> Color(0xFF5D4E37)
            ReaderTheme.NIGHT -> Color(0xFFE6E6E6)
        }

        _settings.value = currentSettings.copy(
            backgroundColor = backgroundColor,
            textColor = textColor
        )
    }

    fun updateBookSettings(bookId: Long) {
        // Load book-specific settings from preferences
        // For now, use default settings
        updateSettings(ReaderSettings())
    }

    fun toggleFontSelector() {
        _uiState.value = _uiState.value.copy(
            showFontSelector = !_uiState.value.showFontSelector
        )
    }

    fun hideFontSelector() {
        _uiState.value = _uiState.value.copy(showFontSelector = false)
    }

    fun toggleThemeSelector() {
        _uiState.value = _uiState.value.copy(
            showThemeSelector = !_uiState.value.showThemeSelector
        )
    }

    fun hideThemeSelector() {
        _uiState.value = _uiState.value.copy(showThemeSelector = false)
    }

    fun showChapterList() {
        _uiState.value = _uiState.value.copy(showChapterList = true)
    }

    fun hideChapterList() {
        _uiState.value = _uiState.value.copy(showChapterList = false)
    }

    fun showSettings() {
        // Show additional settings panel
        showChapterList()
    }

    fun toggleAutoScroll() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isAutoScrolling = !currentState.isAutoScrolling
        )

        if (_uiState.value.isAutoScrolling) {
            startAutoScroll()
        } else {
            stopAutoScroll()
        }
    }

    private fun startAutoScroll() {
        viewModelScope.launch {
            while (_uiState.value.isAutoScrolling) {
                delay(2000) // Scroll every 2 seconds
                // Auto-scroll logic would be implemented here
            }
        }
    }

    private fun stopAutoScroll() {
        // Stop auto-scroll coroutine
    }

    fun handleGesture(gestureInfo: GestureInfo) {
        when (gestureInfo) {
            is GestureInfo.TapLeft -> {
                previousChapter()
            }
            is GestureInfo.TapRight -> {
                nextChapter()
            }
            is GestureInfo.SwipeLeft -> {
                nextChapter()
            }
            is GestureInfo.SwipeRight -> {
                previousChapter()
            }
            is GestureInfo.Pan -> {
                // Handle pan gestures for scrolling
                scrollBy(gestureInfo.deltaY)
            }
            is GestureInfo.Scale -> {
                // Handle pinch-to-zoom for font size adjustment
                adjustFontSize(gestureInfo.scale)
            }
        }
    }

    fun scrollBy(delta: Float) {
        // Handle manual scrolling
        // This would integrate with the scroll state of the reader content
    }

    fun retryLoad() {
        val currentPosition = _uiState.value.currentChapterPosition
        loadChapter(currentPosition)
    }

    private fun adjustFontSize(scale: Float) {
        val currentSettings = _settings.value
        val newSize = (currentSettings.fontSize * scale).coerceIn(12f, 32f)
        updateSettings(currentSettings.copy(fontSize = newSize))
    }

    private fun generateMockChapters(): List<ChapterData> {
        return (1..20).map { index ->
            ChapterData(
                title = "第${index}章：${generateChapterTitle(index)}",
                content = generateChapterContent(index)
            )
        }
    }

    private fun generateChapterTitle(index: Int): String {
        val titles = listOf(
            "序章", "启程", "相遇", "离别", "重逢",
            "冒险", "挑战", "成长", "抉择", "胜利",
            "考验", "突破", "觉醒", "传承", "使命",
            "真相", "和解", "新生", "永恒", "归途"
        )
        return titles[index % titles.size]
    }

    private fun generateChapterContent(index: Int): String {
        return """
            ${"    ".repeat(3)}${generateChapterTitle(index)}开始了新的篇章。这是一个充满悬念和惊喜的故事，让读者欲罢不能。

            ${"    ".repeat(3)}故事的主角踏上了新的旅程，面对未知的挑战和困难。每一个选择都可能改变命运的走向。

            ${"    ".repeat(3)}在这个充满魔法与奇迹的世界里，友情、爱情和亲情交织在一起，构成了这个动人的故事。

            ${"    ".repeat(3)}随着情节的推进，更多的秘密被揭开，真相也逐渐浮出水面。读者将跟随主角一起经历成长的阵痛。

            ${"    ".repeat(3)}每一次转折都让人意想不到，每一次选择都充满挑战。这是一个关于勇气、智慧和坚持的故事。

            ${"    ".repeat(3)}在经历了重重考验之后，主角终于明白了自己真正的使命。未来的道路虽然艰难，但希望永远存在。

            ${"    ".repeat(3)}这是一个关于成长、关于梦想、关于永不放弃信念的故事。相信每个读者都能从中获得力量。
        """.trimIndent()
    }
}

data class TTSEnabled(
    val isEnabled: Boolean,
    val isPaused: Boolean = false
)

data class ReaderUiState(
    val isLoading: Boolean = false,
    val isAutoScrolling: Boolean = false,
    val currentChapter: ChapterData? = null,
    val currentChapterPosition: Int = 1,
    val totalChapters: Int = 0,
    val chapters: List<String> = emptyList(),
    val error: String? = null,
    val showControls: Boolean = true,
    val showFontSelector: Boolean = false,
    val showThemeSelector: Boolean = false,
    val showChapterList: Boolean = false
)

data class ChapterData(
    val title: String,
    val content: String
)