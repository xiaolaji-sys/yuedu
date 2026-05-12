package com.legado.presentation.screens.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.legado.data.database.entities.BookEntity
import com.legado.presentation.screens.read.ReaderScreen

@Composable
fun ReaderDemoScreen() {
    // Mock book data for demonstration
    val demoBook = BookEntity(
        id = 1L,
        bookId = "demo_book",
        name = "沉浸式阅读器演示",
        author = "演示作者",
        coverUrl = null,
        intro = "这是一个展示沉浸式阅读器功能的示例书籍",
        category = "技术文档",
        lastCheckTime = System.currentTimeMillis(),
        updateTime = System.currentTimeMillis(),
        createTime = System.currentTimeMillis(),
        wordCount = 5000,
        chaptersCount = 3,
        toChapterPosition = 1,
        currentChapterPosition = 1,
        sourceId = "demo_source",
        originName = "沉浸式阅读器演示",
        customCover = null
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📖 沉浸式阅读器演示",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "点击下方按钮开始体验沉浸式阅读功能",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Navigate to full reader */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("启动沉浸式阅读器")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "功能预览",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("✅ 全屏沉浸模式", style = MaterialTheme.typography.bodyMedium)
                    Text("✅ 多种翻页动画效果", style = MaterialTheme.typography.bodyMedium)
                    Text("✅ 字体大小和主题自定义", style = MaterialTheme.typography.bodyMedium)
                    Text("✅ 手势交互支持", style = MaterialTheme.typography.bodyMedium)
                    Text("✅ 自动滚动功能", style = MaterialTheme.typography.bodyMedium)
                    Text("✅ 章节快速跳转", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}