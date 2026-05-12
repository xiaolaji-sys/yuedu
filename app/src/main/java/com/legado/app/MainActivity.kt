package com.legado.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.legado.core.theme.LegadoDynamicTheme
import com.legado.data.database.entities.BookEntity
import com.legado.presentation.screens.home.HomeScreen
import com.legado.presentation.navigation.aiDirectoryScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LegadoDynamicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onBookClick = { book ->
                                    // Navigate to reader with selected book
                                    navController.navigate("reader/${book.id}/${book.currentChapterPosition}")
                                },
                                onAIDirectoryClick = {
                                    navController.navigate("ai_directory")
                                }
                            )
                        }

                        aiDirectoryScreen()
                    }
                }
            }
        }
    }
}