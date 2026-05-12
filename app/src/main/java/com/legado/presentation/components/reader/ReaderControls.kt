package com.legado.presentation.components.reader

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.legado.service.TTSService

@Composable
fun ReaderControls(
    currentChapter: Int,
    totalChapters: Int,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onProgressChange: (Float) -> Unit,
    isAutoScrolling: Boolean,
    onToggleAutoScroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Progress bar
            ChapterProgressBar(
                currentChapter = currentChapter,
                totalChapters = totalChapters,
                onProgressChange = onProgressChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous chapter button
                IconButton(
                    onClick = onPreviousChapter,
                    enabled = currentChapter > 1
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上一章",
                        tint = if (currentChapter > 1) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                }

                // Auto-scroll toggle
                AutoScrollButton(
                    isActive = isAutoScrolling,
                    onClick = onToggleAutoScroll,
                    modifier = Modifier.size(48.dp)
                )

                // Next chapter button
                IconButton(
                    onClick = onNextChapter,
                    enabled = currentChapter < totalChapters
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "下一章",
                        tint = if (currentChapter < totalChapters) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter indicator
            Text(
                text = "第 $currentChapter 章 / 共 $totalChapters 章",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
        mutableStateOf(if (totalChapters > 0) currentChapter.toFloat() / totalChapters else 0f)
    }

    LaunchedEffect(currentChapter, totalChapters) {
        if (totalChapters > 0) {
            sliderPosition = currentChapter.toFloat() / totalChapters
        }
    }

    Column(modifier = modifier) {
        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                sliderPosition = value
                val chapterPosition = (value * totalChapters).roundToInt().coerceIn(1, totalChapters)
                onProgressChange(chapterPosition.toFloat())
            },
            valueRange = 0f..1f,
            steps = totalChapters - 2, // Steps between min and max
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    thumbSize = 16.dp
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    drawStopIndicator = null,
                    thumbPath = null
                )
            }
        )

        // Chapter markers for quick navigation
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
                                onProgressChange(chapterNumber.toFloat())
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
private fun AutoScrollButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpeedSelector by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = {
                if (isActive) {
                    showSpeedSelector = !showSpeedSelector
                } else {
                    onClick()
                }
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isActive) "暂停自动滚动" else "开始自动滚动",
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        if (showSpeedSelector && isActive) {
            AutoScrollSpeedSelector(
                onSpeedSelected = { speed ->
                    // Handle speed selection
                    showSpeedSelector = false
                },
                onDismiss = { showSpeedSelector = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-60).dp)
            )
        }
    }
}

@Composable
private fun TTSControlButton(
    isPlaying: Boolean,
    onToggled: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggled,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停朗读" else "开始朗读",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}