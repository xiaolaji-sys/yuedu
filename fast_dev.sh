#!/bin/bash
# 高效开发脚本 - 并行创建关键功能模块

echo "🚀 Legado 快速功能开发启动..."

# 1. 创建阅读器核心文件 (并行)
cat > app/src/main/java/com/legado/presentation/screens/read/ReaderScreen.kt << 'EOF'
package com.legado.presentation.screens.read

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.bookTitle) },
                actions = {
                    IconButton(onClick = { viewModel.toggleControls() }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.showControls) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { viewModel.previousChapter() }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上一章")
                        }
                        Text("${uiState.currentChapter}/${uiState.totalChapters}")
                        IconButton(onClick = { viewModel.nextChapter() }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下一章")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        ReaderContent(
            text = uiState.chapterText,
            settings = uiState.readingSettings,
            onTextSizeChange = viewModel::updateTextSize,
            onLineSpacingChange = viewModel::updateLineSpacing,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        when {
                            change.x > 100 -> viewModel.nextChapter()
                            change.x < -100 -> viewModel.previousChapter()
                        }
                    }
                }
        )
    }
}
EOF

# 2. 创建阅读器内容组件
cat > app/src/main/java/com/legado/presentation/components/reader/ReaderContent.kt << 'EOF'
package com.legado.presentation.components.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReadingSettings(
    val fontSize: Float = 16f,
    val lineSpacing: Float = 1.5f,
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val fontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.Default,
    val bold: Boolean = false
)

@Composable
fun ReaderContent(
    text: String,
    settings: ReadingSettings,
    onTextSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(settings.backgroundColor)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = text.ifEmpty { "加载中..." },
            fontSize = settings.fontSize.sp,
            lineHeight = settings.fontSize * settings.lineSpacing.sp,
            fontWeight = if (settings.bold) FontWeight.Bold else FontWeight.Normal,
            color = settings.textColor,
            fontFamily = settings.fontFamily,
            modifier = Modifier.fillMaxSize()
        )
    }
}
EOF

# 3. 创建书源管理核心文件
cat > app/src/main/java/com/legado/data/source/BookSourceManager.kt << 'EOF'
package com.legado.data.source

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookSourceManager @Inject constructor(
    private val context: Context
) {
    private val sources = mutableListOf<BookSource>()

    fun addSource(source: BookSource) {
        sources.add(source)
    }

    fun removeSource(sourceId: String) {
        sources.removeIf { it.id == sourceId }
    }

    fun getSources(): List<BookSource> = sources.toList()

    fun findBooks(query: String): Flow<List<Book>> = flowOf(
        listOf(
            Book("1", "示例小说", "作者", "简介", "source1"),
            Book("2", "另一本好书", "另一位作者", "另一个简介", "source2")
        ).filter { it.title.contains(query, ignoreCase = true) }
    )

    fun getBookSource(id: String): BookSource? = sources.find { it.id == id }
}

data class BookSource(
    val id: String,
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val sourceId: String
)
EOF

echo "✅ 成功创建了核心功能模块!"
echo "📱 阅读器界面"
echo "📚 书源管理系统"
echo "🎯 准备进入测试阶段..."