package com.legado.presentation.components.player

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Audio player UI component for TTS functionality
 */
@Composable
fun AudioPlayer(
    ttsService: TTSService,
    currentChapter: Int,
    totalChapters: Int,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onProgressChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showSpeedControl: Boolean = true,
    showVoiceSelector: Boolean = true,
    showVolumeSlider: Boolean = true
) {
    val isInitialized by ttsService.isInitialized.collectAsState()
    val isPlaying by ttsService.isPlaying.collectAsState()
    val volume by ttsService.volume.collectAsState()
    val speechRate by ttsService.speechRate.collectAsState()
    val pitch by ttsService.pitch.collectAsState()
    val availableVoices by ttsService.availableVoices.collectAsState()
    val currentVoice by ttsService.currentVoice.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status indicator
            if (!isInitialized) {
                Text(
                    text = "TTS服务未初始化",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main control buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous chapter button
                IconButton(
                    onClick = {
                        if (ttsService.previousChapter()) {
                            onPreviousChapter()
                        }
                    },
                    enabled = currentChapter > 1 && isInitialized
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上一章",
                        tint = if (currentChapter > 1 && isInitialized) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                }

                // Play/Pause button
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            ttsService.pauseReading()
                        } else {
                            if (ttsService.getCurrentChapterIndex() == currentChapter - 1) {
                                ttsService.resumeReading()
                            } else {
                                ttsService.startReading(currentChapter - 1)
                            }
                        }
                    },
                    enabled = isInitialized
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = if (isInitialized) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next chapter button
                IconButton(
                    onClick = {
                        if (ttsService.nextChapter()) {
                            onNextChapter()
                        }
                    },
                    enabled = currentChapter < totalChapters && isInitialized
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "下一章",
                        tint = if (currentChapter < totalChapters && isInitialized) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress indicator
            ChapterProgressBar(
                currentChapter = currentChapter,
                totalChapters = totalChapters,
                onProgressChange = { position ->
                    val chapterPosition = (position * totalChapters).roundToInt().coerceIn(1, totalChapters)
                    onProgressChange(chapterPosition)
                    ttsService.goToChapter(chapterPosition - 1)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Settings section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Volume control
                if (showVolumeSlider) {
                    VolumeSlider(
                        volume = volume,
                        onVolumeChanged = { ttsService.setVolume(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Speed control
                if (showSpeedControl) {
                    SpeechRateButton(
                        rate = speechRate,
                        onRateChanged = { ttsService.setSpeechRate(it) },
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Voice selector
                if (showVoiceSelector && availableVoices.isNotEmpty()) {
                    VoiceSelector(
                        voices = availableVoices,
                        currentVoice = currentVoice ?: "",
                        onVoiceSelected = { voiceName ->
                            ttsService.setVoice(voiceName)
                        },
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第 $currentChapter 章 / 共 $totalChapters 章",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = when {
                        !isInitialized -> "准备中..."
                        isPlaying -> "播放中"
                        else -> "已暂停"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        !isInitialized -> MaterialTheme.colorScheme.error
                        isPlaying -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun ChapterProgressBar(
    currentChapter: Int,
    totalChapters: Int,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember(currentChapter, totalChapters) {
        mutableStateOf(if (totalChapters > 0) (currentChapter - 1).toFloat() / totalChapters else 0f)
    }

    LaunchedEffect(currentChapter, totalChapters) {
        if (totalChapters > 0) {
            sliderPosition = (currentChapter - 1).toFloat() / totalChapters
        }
    }

    Column(modifier = modifier) {
        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                sliderPosition = value
                onProgressChange(value)
            },
            valueRange = 0f..1f,
            steps = totalChapters - 2,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    thumbSize = 16.dp
                )
            }
        )

        // Quick navigation markers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(minOf(totalChapters, 5)) { index ->
                val chapterNumber = (index + 1) * (totalChapters / 5)
                if (chapterNumber <= totalChapters) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (chapterNumber == currentChapter) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                onProgressChange((chapterNumber - 1).toFloat() / totalChapters)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chapterNumber.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (chapterNumber == currentChapter) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeSlider(
    volume: Float,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.VolumeUp,
                contentDescription = "音量",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "音量: ${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Slider(
                    value = volume,
                    onValueChange = onVolumeChanged,
                    valueRange = TTSService.MIN_VOLUME..TTSService.MAX_VOLUME,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

@Composable
private fun SpeechRateButton(
    rate: Float,
    onRateChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Speed,
                contentDescription = "语速",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-80).dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "语速: ${String.format("%.1f", rate)}x",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    val rates = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    rates.forEach { rateValue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRateChanged(rateValue) }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = abs(rate - rateValue) < 0.01f,
                                onClick = { onRateChanged(rateValue) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${rateValue}x",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceSelector(
    voices: List<String>,
    currentVoice: String,
    onVoiceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.RecordVoiceOver,
                contentDescription = "语音",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-120).dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "选择语音",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    voices.forEach { voice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVoiceSelected(voice) }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = voice == currentVoice,
                                onClick = { onVoiceSelected(voice) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = voice.substringAfterLast('/'),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}