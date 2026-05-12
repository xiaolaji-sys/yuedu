package com.legado.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * TTS (Text-to-Speech) Service for book reading functionality
 */
class TTSService(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    private val _currentVoice = MutableStateFlow<String?>(null)
    val currentVoice: StateFlow<String?> = _currentVoice.asStateFlow()

    // Utterance tracking for chapter management
    private var currentChapterIndex = 0
    private var chapters = mutableListOf<String>()
    private var isPaused = false

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            e.printStackTrace()
            _isInitialized.value = false
        }
    }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                // Set language to Chinese (Simplified)
                val result = tts?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English if Chinese not supported
                    tts?.setLanguage(Locale.US)
                }

                // Get available voices
                tts?.voices?.let { voices ->
                    _availableVoices.value = voices.map { it.name }
                    if (_availableVoices.value.isNotEmpty()) {
                        _currentVoice.value = _availableVoices.value.first()
                    }
                }

                _isInitialized.value = true
            }
            else -> {
                _isInitialized.value = false
            }
        }
    }

    /**
     * Add chapters for sequential reading
     */
    fun setChapters(chapterTexts: List<String>) {
        chapters.clear()
        chapters.addAll(chapterTexts)
        currentChapterIndex = 0
    }

    /**
     * Start reading from a specific chapter
     */
    fun startReading(startFromChapter: Int = 0) {
        if (!isInitialized.value || chapters.isEmpty()) return

        currentChapterIndex = startFromChapter.coerceIn(0, chapters.size - 1)
        readCurrentChapter()
    }

    /**
     * Read current chapter
     */
    private fun readCurrentChapter() {
        if (!isInitialized.value || currentChapterIndex >= chapters.size) return

        val chapterText = chapters[currentChapterIndex]
        speakText(chapterText, "chapter_${currentChapterIndex + 1}")
    }

    /**
     * Speak text with utterance tracking
     */
    private fun speakText(text: String, utteranceId: String) {
        if (!isInitialized.value) return

        stopSpeaking()

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isPlaying.value = true
                isPaused = false
            }

            override fun onDone(utteranceId: String?) {
                _isPlaying.value = false
                // Automatically proceed to next chapter
                nextChapter()
            }

            override fun onError(utteranceId: String?) {
                _isPlaying.value = false
                // Handle error and continue to next chapter or stop
                nextChapter()
            }

            @Suppress("DEPRECATION")
            override fun onDone(utteranceId: String, isError: Boolean) {
                onDone(utteranceId)
            }

            @Suppress("DEPRECATION")
            override fun onError(utteranceId: String, errorCode: Int) {
                onError(utteranceId)
            }
        })

        // Set TTS parameters
        tts?.setPitch(_pitch.value)
        tts?.setSpeechRate(_speechRate.value)
        tts?.setVolume(_volume.value)

        // Speak with utterance ID for progress tracking
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Pause current reading
     */
    fun pauseReading() {
        if (_isPlaying.value && !isPaused) {
            tts?.pause()
            isPaused = true
            _isPlaying.value = false
        }
    }

    /**
     * Resume reading
     */
    fun resumeReading() {
        if (isPaused) {
            // Note: Android TTS doesn't support perfect resume,
            // so we restart from current position
            readCurrentChapter()
            isPaused = false
        }
    }

    /**
     * Stop current reading
     */
    fun stopSpeaking() {
        tts?.stop()
        _isPlaying.value = false
        isPaused = false
    }

    /**
     * Next chapter
     */
    fun nextChapter(): Boolean {
        if (currentChapterIndex < chapters.size - 1) {
            currentChapterIndex++
            readCurrentChapter()
            return true
        }
        return false
    }

    /**
     * Previous chapter
     */
    fun previousChapter(): Boolean {
        if (currentChapterIndex > 0) {
            currentChapterIndex--
            readCurrentChapter()
            return true
        }
        return false
    }

    /**
     * Go to specific chapter
     */
    fun goToChapter(chapterIndex: Int): Boolean {
        if (chapterIndex in 0 until chapters.size) {
            currentChapterIndex = chapterIndex
            readCurrentChapter()
            return true
        }
        return false
    }

    /**
     * Adjust volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0.0f, 1.0f)
        _volume.value = clampedVolume
        tts?.setVolume(clampedVolume)
    }

    /**
     * Adjust speech rate (0.5 to 2.0)
     */
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = clampedRate
        tts?.setSpeechRate(clampedRate)
    }

    /**
     * Adjust pitch (0.5 to 2.0)
     */
    fun setPitch(pitchValue: Float) {
        val clampedPitch = pitchValue.coerceIn(0.5f, 2.0f)
        _pitch.value = clampedPitch
        tts?.setPitch(clampedPitch)
    }

    /**
     * Set voice by name
     */
    fun setVoice(voiceName: String): Boolean {
        val voices = tts?.voices ?: return false
        val voice = voices.find { it.name == voiceName }
        return if (voice != null) {
            tts?.voice = voice
            _currentVoice.value = voiceName
            true
        } else {
            false
        }
    }

    /**
     * Get current chapter index
     */
    fun getCurrentChapterIndex(): Int {
        return currentChapterIndex
    }

    /**
     * Get total number of chapters
     */
    fun getTotalChapters(): Int {
        return chapters.size
    }

    /**
     * Check if currently paused
     */
    fun isPaused(): Boolean {
        return isPaused
    }

    /**
     * Shutdown TTS service
     */
    fun shutdown() {
        tts?.shutdown()
        _isInitialized.value = false
        _isPlaying.value = false
    }

    companion object {
        const val MIN_VOLUME = 0.0f
        const val MAX_VOLUME = 1.0f
        const val MIN_SPEECH_RATE = 0.5f
        const val MAX_SPEECH_RATE = 2.0f
        const val MIN_PITCH = 0.5f
        const val MAX_PITCH = 2.0f
    }
}