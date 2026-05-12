package com.legado.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legado.ai.DirectoryGenerator
import com.legado.data.model.AIResponse
import com.legado.data.model.AIGenerationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AIDirectoryUiState {
    data object Idle : AIDirectoryUiState()
    data object Loading : AIDirectoryUiState()
    data class Success(val data: com.legado.data.model.AIDirectoryData) : AIDirectoryUiState()
    data class Error(val message: String) : AIDirectoryUiState()
}

@HiltViewModel
class AIDirectoryViewModel @Inject constructor(
    private val directoryGenerator: DirectoryGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow<AIDirectoryUiState>(AIDirectoryUiState.Idle)
    val uiState: StateFlow<AIDirectoryUiState> = _uiState.asStateFlow()

    fun generateDirectory() {
        _uiState.value = AIDirectoryUiState.Loading

        viewModelScope.launch {
            try {
                // In a real app, you would get these from user input
                val request = AIGenerationRequest(
                    bookTitle = "示例书籍",
                    bookDescription = "这是一本示例小说",
                    genre = "玄幻",
                    targetAudience = "成年读者"
                )

                // For demo purposes, use local generation
                // In production, you would use:
                // val result = directoryGenerator.generateDirectory(request, apiKey)
                // if (result.isSuccess) {
                //     _uiState.value = AIDirectoryUiState.Success(result.getOrNull()?.data)
                // } else {
                //     _uiState.value = AIDirectoryUiState.Error(result.exceptionOrNull()?.message ?: "生成失败")
                // }

                val response = directoryGenerator.generateLocalDirectory(
                    bookTitle = request.bookTitle,
                    bookDescription = request.bookDescription,
                    chapterCount = 12
                )

                if (response.success && response.data != null) {
                    _uiState.value = AIDirectoryUiState.Success(response.data)
                } else {
                    _uiState.value = AIDirectoryUiState.Error(response.message ?: "生成失败")
                }
            } catch (e: Exception) {
                _uiState.value = AIDirectoryUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun regenerateDirectory() {
        generateDirectory()
    }

    fun resetState() {
        _uiState.value = AIDirectoryUiState.Idle
    }
}