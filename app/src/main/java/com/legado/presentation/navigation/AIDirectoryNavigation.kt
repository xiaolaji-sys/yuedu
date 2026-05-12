package com.legado.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.legado.presentation.screens.ai.AIDirectoryScreen

const val AI_DIRECTORY_ROUTE = "ai_directory"

fun NavController.navigateToAIDirectory() {
    navigate(AI_DIRECTORY_ROUTE)
}

fun NavGraphBuilder.aiDirectoryScreen() {
    composable(AI_DIRECTORY_ROUTE) {
        AIDirectoryScreen()
    }
}