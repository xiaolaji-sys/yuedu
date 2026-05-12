package com.legado.presentation.components.reader

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legado.presentation.screens.read.ReaderSettings
import com.legado.presentation.screens.read.PageTransition
import kotlin.math.abs

@Composable
fun ReaderContent(
    content: String,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
    onScroll: (Float) -> Unit = {},
    onGesture: (GestureInfo) -> Unit = {}
) {
    val density = LocalDensity.current

    // Split content into paragraphs for better reading experience
    val paragraphs = remember(content) {
        content.split("\n\n").filter { it.isNotBlank() }
    }

    // Handle auto-scroll
    LaunchedEffect(settings.autoScrollSpeed) {
        if (settings.autoScrollSpeed > 0) {
            // Auto-scrolling logic would be implemented here
            // For now, we'll just use manual scrolling
        }
    }

    Box(modifier = modifier) {
        when (settings.pageTransition) {
            PageTransition.SLIDE_HORIZONTAL -> {
                HorizontalPageReader(
                    paragraphs = paragraphs,
                    settings = settings,
                    onScroll = onScroll,
                    onGesture = onGesture,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PageTransition.SLIDE_VERTICAL -> {
                VerticalPageReader(
                    paragraphs = paragraphs,
                    settings = settings,
                    onScroll = onScroll,
                    onGesture = onGesture,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PageTransition.FADE -> {
                FadePageReader(
                    paragraphs = paragraphs,
                    settings = settings,
                    onScroll = onScroll,
                    onGesture = onGesture,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PageTransition.NONE -> {
                SimpleReader(
                    content = content,
                    settings = settings,
                    onScroll = onScroll,
                    onGesture = onGesture,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalPageReader(
    paragraphs: List<String>,
    settings: ReaderSettings,
    onScroll: (Float) -> Unit,
    onGesture: (GestureInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 8 // Adjust based on content and font size

    val totalPages = remember(paragraphs.size, itemsPerPage) {
        (paragraphs.size + itemsPerPage - 1) / itemsPerPage
    }

    // Calculate page content
    val startIndex = currentPage * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, paragraphs.size)
    val pageParagraphs = paragraphs.subList(startIndex, endIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(settings.backgroundColor)
    ) {
        // Reading area
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(pageParagraphs) { index, paragraph ->
                ParagraphText(
                    text = paragraph,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Page indicator
        if (settings.showPageNumbers && totalPages > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalPages) { page ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (page == currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                },
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }

    // Gesture handling for page turning
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Left side for next page, right side for previous page
                        val threshold = size.width * 0.3f
                        if (offset.x < threshold && currentPage > 0) {
                            currentPage--
                            onGesture(GestureInfo.TapLeft)
                        } else if (offset.x > size.width - threshold && currentPage < totalPages - 1) {
                            currentPage++
                            onGesture(GestureInfo.TapRight)
                        }
                    },
                    onPanEnd = { velocity ->
                        // Swipe to turn pages
                        if (velocity.x > 500 && currentPage > 0) {
                            currentPage--
                            onGesture(GestureInfo.SwipeLeft)
                        } else if (velocity.x < -500 && currentPage < totalPages - 1) {
                            currentPage++
                            onGesture(GestureInfo.SwipeRight)
                        }
                    }
                )
            }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerticalPageReader(
    paragraphs: List<String>,
    settings: ReaderSettings,
    onScroll: (Float) -> Unit,
    onGesture: (GestureInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var scrollState by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(settings.backgroundColor)
            .verticalScroll(
                rememberScrollState(),
                enabled = true
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            paragraphs.forEach { paragraph ->
                ParagraphText(
                    text = paragraph,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Gesture handling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Tap to scroll down/up
                        val scrollAmount = size.height * 0.3f
                        if (offset.y < scrollAmount) {
                            // Scroll up
                            onScroll(-scrollAmount)
                        } else if (offset.y > size.height - scrollAmount) {
                            // Scroll down
                            onScroll(scrollAmount)
                        }
                    }
                )
            }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FadePageReader(
    paragraphs: List<String>,
    settings: ReaderSettings,
    onScroll: (Float) -> Unit,
    onGesture: (GestureInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentParagraph by remember { mutableStateOf(0) }
    val totalParagraphs = paragraphs.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(settings.backgroundColor)
    ) {
        AnimatedVisibility(
            visible = currentParagraph < totalParagraphs,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ParagraphText(
                    text = paragraphs[currentParagraph],
                    settings = settings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentParagraph + 1).toFloat() / totalParagraphs },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }

    // Gesture handling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (offset.x < size.width * 0.5f && currentParagraph > 0) {
                            currentParagraph--
                            onGesture(GestureInfo.TapLeft)
                        } else if (offset.x > size.width * 0.5f && currentParagraph < totalParagraphs - 1) {
                            currentParagraph++
                            onGesture(GestureInfo.TapRight)
                        }
                    }
                )
            }
    )
}

@Composable
private fun SimpleReader(
    content: String,
    settings: ReaderSettings,
    onScroll: (Float) -> Unit,
    onGesture: (GestureInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(settings.backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParagraphText(
                text = content,
                settings = settings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ParagraphText(
    text: String,
    settings: ReaderSettings,
    modifier: Modifier = Modifier
) {
    Text(
        text = AnnotatedString(text),
        modifier = modifier,
        style = TextStyle(
            fontSize = settings.fontSize.sp,
            lineHeight = (settings.fontSize * settings.lineHeight).sp,
            fontFamily = settings.fontFamily,
            color = settings.textColor,
            textAlign = TextAlign.Justify,
            letterSpacing = 0.5.sp
        ),
        softWrap = true,
        overflow = TextOverflow.Visible
    )
}

// Gesture info classes
sealed class GestureInfo {
    object TapLeft : GestureInfo()
    object TapRight : GestureInfo()
    object SwipeLeft : GestureInfo()
    object SwipeRight : GestureInfo()
    data class Pan(val deltaX: Float, val deltaY: Float) : GestureInfo()
    data class Scale(val scale: Float) : GestureInfo()
}