package com.legado.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.legado.presentation.screens.source.BookSourceScreen

const val BOOK_SOURCE_ROUTE = "book_source"

fun NavController.navigateToBookSource() {
    navigate(BOOK_SOURCE_ROUTE)
}

fun NavGraphBuilder.bookSourceScreen() {
    composable(BOOK_SOURCE_ROUTE) {
        BookSourceScreen()
    }
}