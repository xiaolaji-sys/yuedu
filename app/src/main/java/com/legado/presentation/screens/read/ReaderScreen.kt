package com.legado.presentation.screens.read

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.legado.data.database.entities.BookEntity
import com.legado.presentation.components.reader.ReaderControls
import com.legado.presentation.components.reader.ReaderContent

enum class ReaderTheme {
    LIGHT, DARK, SEPIA, NIGHT
}

data class ReaderSettings(
    val fontSize: Float = 16f,
    val lineHeight: Float = 1.6f,
    val fontFamily: FontFamily = FontFamily.Default,
    val theme: ReaderTheme = ReaderTheme.LIGHT,
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val showPageNumbers: Boolean = true,
    val autoScrollSpeed: Float = 0f,
    val pageTransition: PageTransition = PageTransition.SLIDE_HORIZONTAL
)

enum class PageTransition {
    SLIDE_HORIZONTAL, SLIDE_VERTICAL, FADE, NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: BookEntity,
    initialChapterPosition: Int = 1,
    onBackClick: () -> Unit = {},
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Initialize chapter when screen loads
    LaunchedEffect(book.id) {
        viewModel.loadChapter(book.id, initialChapterPosition)
    }

    // Update settings when book changes
    LaunchedEffect(book.id) {
        viewModel.updateBookSettings(book.id)
    }

    // Handle system UI visibility for immersive experience
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(viewModel.nestedScrollConnection),
        topBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${book.name} - ${uiState.currentChapter?.title ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFontSelector() }) {
                            Icon(Icons.Default.TextFields, contentDescription = "字体设置")
                        }
                        IconButton(onClick = { viewModel.toggleThemeSelector() }) {
                            Icon(Icons.Default.Palette, contentDescription = "主题设置")
                        }
                        IconButton(onClick = { viewModel.showSettings() }) {
                            Icon(Icons.Default.Settings, contentDescription = "更多设置")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (settings.theme == ReaderTheme.DARK) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        }
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ReaderControls(
                    currentChapter = uiState.currentChapterPosition,
                    totalChapters = uiState.totalChapters,
                    onPreviousChapter = { viewModel.previousChapter() },
                    onNextChapter = { viewModel.nextChapter() },
                    onProgressChange = { progress -> viewModel.goToChapter(progress) },
                    isAutoScrolling = uiState.isAutoScrolling,
                    onToggleAutoScroll = { viewModel.toggleAutoScroll() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main reading content
            ReaderContent(
                content = uiState.currentChapter?.content ?: "",
                settings = settings,
                modifier = Modifier.fillMaxSize(),
                onScroll = { delta -> viewModel.scrollBy(delta) },
                onGesture = { gestureInfo -> viewModel.handleGesture(gestureInfo) }
            )

            // Chapter navigation overlay
            if (uiState.showChapterList) {
                ChapterListOverlay(
                    chapters = uiState.chapters,
                    currentPosition = uiState.currentChapterPosition,
                    onChapterSelected = { position -> viewModel.goToChapter(position) },
                    onDismiss = { viewModel.hideChapterList() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Settings panels
            if (uiState.showFontSelector) {
                FontSelectorPanel(
                    settings = settings,
                    onSettingsChanged = viewModel::updateSettings,
                    onDismiss = { viewModel.hideFontSelector() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (uiState.showThemeSelector) {
                ThemeSelectorPanel(
                    settings = settings,
                    onSettingsChanged = viewModel::updateSettings,
                    onDismiss = { viewModel.hideThemeSelector() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retryLoad() }
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterListOverlay(
    chapters: List<String>,
    currentPosition: Int,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "章节目录",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Chapter list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(chapters.size) { index ->
                    val position = index + 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (position == currentPosition) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            }
                        ),
                        onClick = { onChapterSelected(position) }
                    ) {
                        Text(
                            text = chapters[index],
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (position == currentPosition) {
                                MaterialTheme.colorScheme.onPrimaryContainer
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
private fun FontSelectorPanel(
    settings: ReaderSettings,
    onSettingsChanged: (ReaderSettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSettings by remember(settings) { mutableStateOf(settings) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "字体设置",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = {
                    onSettingsChanged(tempSettings)
                    onDismiss()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "确认")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font size slider
            Text("字体大小")
            Slider(
                value = tempSettings.fontSize / 24f, // Normalize to 0-1 range
                onValueChange = { value ->
                    tempSettings = tempSettings.copy(fontSize = value * 24f)
                },
                valueRange = 0.5f..2.0f,
                steps = 14
            )
            Text("${tempSettings.fontSize.toInt()}sp", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Line height slider
            Text("行高")
            Slider(
                value = tempSettings.lineHeight,
                onValueChange = { tempSettings = tempSettings.copy(lineHeight = it) },
                valueRange = 1.2f..2.5f,
                steps = 12
            )
            Text("${String.format("%.1f", tempSettings.lineHeight)}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Preview text
            Text(
                text = "这是一段预览文字，用于查看字体效果。",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = tempSettings.fontSize.sp,
                    lineHeight = (tempSettings.fontSize * tempSettings.lineHeight).sp
                ),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun ThemeSelectorPanel(
    settings: ReaderSettings,
    onSettingsChanged: (ReaderSettings) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSettings by remember(settings) { mutableStateOf(settings) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "阅读主题",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = {
                    onSettingsChanged(tempSettings)
                    onDismiss()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "确认")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme options
            val themes = listOf(
                ReaderTheme.LIGHT to "明亮模式",
                ReaderTheme.DARK to "暗黑模式",
                ReaderTheme.SEPIA to "护眼模式",
                ReaderTheme.NIGHT to "夜间模式"
            )

            themes.forEach { (theme, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tempSettings = tempSettings.copy(theme = theme) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tempSettings.theme == theme,
                        onClick = { tempSettings = tempSettings.copy(theme = theme) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview text
            Text(
                text = "这是一段预览文字，用于查看主题效果。",
                style = MaterialTheme.typography.bodyLarge.copy(color = tempSettings.textColor),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}