package com.legado.presentation.screens.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.legado.presentation.components.player.AudioPlayer
import com.legado.service.TTSService

/**
 * Demo screen showcasing TTS functionality
 */
@Composable
fun TTSDemoScreen(
    ttsService: TTSService = hiltViewModel<ReaderViewModel>().ttsService,
    modifier: Modifier = Modifier
) {
    var currentChapter by remember { mutableStateOf(1) }
    val totalChapters = 20

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TTS 听书功能演示",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "这是一个演示文本，用于展示TTS听书功能的各种特性。",
            style = MaterialTheme.typography.bodyLarge
        )

        // Audio Player Component
        AudioPlayer(
            ttsService = ttsService,
            currentChapter = currentChapter,
            totalChapters = totalChapters,
            onPreviousChapter = { if (currentChapter > 1) currentChapter-- },
            onNextChapter = { if (currentChapter < totalChapters) currentChapter++ },
            onProgressChange = { chapter -> currentChapter = chapter },
            modifier = Modifier.fillMaxWidth(),
            showSpeedControl = true,
            showVoiceSelector = true,
            showVolumeSlider = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Additional Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Load sample chapters for TTS
                    val sampleChapters = listOf(
                        "第一章：这是一个关于成长的故事。主人公踏上了新的旅程，面对未知的挑战。",
                        "第二章：在这个充满魔法的世界里，友情和勇气是最大的力量。",
                        "第三章：每一次选择都可能改变命运的走向，主角必须做出艰难的决定。"
                    )
                    ttsService.setChapters(sampleChapters)
                }
            ) {
                Text("加载示例章节")
            }

            Button(
                onClick = {
                    // Reset to chapter 1
                    currentChapter = 1
                }
            ) {
                Text("重置")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "当前状态",
                    style = MaterialTheme.typography.titleSmall
                )

                val isPlaying by ttsService.isPlaying.collectAsState()
                val volume by ttsService.volume.collectAsState()
                val speechRate by ttsService.speechRate.collectAsState()

                Text("播放状态: ${if (isPlaying) "播放中" else "已停止"}")
                Text("音量: ${(volume * 100).toInt()}%")
                Text("语速: ${String.format("%.1f", speechRate)}x")

                if (!ttsService.isInitialized.value) {
                    Text(
                        text = "TTS服务未初始化",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}