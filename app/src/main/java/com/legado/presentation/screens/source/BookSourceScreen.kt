package com.legado.presentation.screens.source

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legado.data.network.model.Book
import com.legado.data.source.BookSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSourceScreen(
    viewModel: BookSourceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书源管理") },
                actions = {
                    IconButton(onClick = { viewModel.refreshSources() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BookSourceUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BookSourceUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.bookSources) { source ->
                        BookSourceCard(
                            source = source,
                            onToggleEnabled = { viewModel.toggleSourceEnabled(source.id) },
                            onEdit = { viewModel.editSource(source) },
                            onDelete = { viewModel.deleteSource(source.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            is BookSourceUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "加载失败",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.loadBookSources() }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }

    // Show add/edit dialog
    if (uiState.showDialog) {
        AddEditBookSourceDialog(
            source = uiState.editingSource,
            onDismiss = { viewModel.hideDialog() },
            onSave = { source ->
                if (uiState.editingSource == null) {
                    viewModel.addSource(source)
                } else {
                    viewModel.updateSource(source)
                }
            }
        )
    }
}

@Composable
fun BookSourceCard(
    source: BookSource,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = source.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!source.description.isNullOrBlank()) {
                        Text(
                            text = source.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Switch(
                    checked = source.enabled,
                    onCheckedChange = { onToggleEnabled() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("编辑")
                }
                TextButton(onClick = onDelete) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddEditBookSourceDialog(
    source: BookSource?,
    onDismiss: () -> Unit,
    onSave: (BookSource) -> Unit
) {
    var name by remember { mutableStateOf(source?.name ?: "") }
    var baseUrl by remember { mutableStateOf(source?.baseUrl ?: "") }
    var description by remember { mutableStateOf(source?.description ?: "") }
    var rules by remember { mutableStateOf(source?.rules ?: emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (source == null) "添加书源" else "编辑书源") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("书源名称") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("基础URL") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    maxLines = 3
                )

                Text(
                    text = "解析规则（JSON格式，可选）",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rules.joinToString("\n") { "${it.selector} -> ${it.attribute}" },
                    onValueChange = { /* Parse rules here */ },
                    placeholder = { Text("css选择器 -> 属性名\n.example -> title\n.book-item -> author") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank()) {
                        val newSource = BookSource(
                            id = source?.id ?: "source_${System.currentTimeMillis()}",
                            name = name.trim(),
                            baseUrl = baseUrl.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            rules = rules
                        )
                        onSave(newSource)
                    }
                },
                enabled = name.isNotBlank() && baseUrl.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ViewModel and UI State classes would be defined in a separate file
sealed class BookSourceUiState {
    object Loading : BookSourceUiState()
    data class Success(val bookSources: List<BookSource>) : BookSourceUiState()
    data class Error(val message: String) : BookSourceUiState()
}

object Icons {
    val Refresh = androidx.compose.material.icons.Icons.Default.Refresh
}