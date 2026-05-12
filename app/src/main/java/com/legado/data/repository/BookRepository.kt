package com.legado.data.repository

import com.legado.data.network.api.BookSourceApi
import com.legado.data.network.model.BookResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookSourceApi: BookSourceApi
) {

    suspend fun getBooks(
        page: Int = 1,
        limit: Int = 20,
        source: String? = null
    ): Result<BookResponse> {
        return try {
            val response = bookSourceApi.getBooks(page, limit, source)
            if (response.isSuccessful) {
                Result.success(response.body() ?: BookResponse())
            } else {
                Result.failure(Exception("Failed to fetch books: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkError(e))
        }
    }

    suspend fun getBookById(bookId: String): Result<BookResponse> {
        return try {
            val response = bookSourceApi.getBookById(bookId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: BookResponse())
            } else {
                Result.failure(Exception("Failed to fetch book: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkError(e))
        }
    }

    suspend fun searchBooks(
        query: String,
        source: String? = null
    ): Result<BookResponse> {
        return try {
            val response = bookSourceApi.searchBooks(query, source)
            if (response.isSuccessful) {
                Result.success(response.body() ?: BookResponse())
            } else {
                Result.failure(Exception("Failed to search books: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkError(e))
        }
    }

    suspend fun getBookSources(): Result<List<String>> {
        return try {
            val response = bookSourceApi.getBookSources()
            if (response.isSuccessful) {
                val sources = response.body() ?: emptyList()
                Result.success(sources)
            } else {
                Result.failure(Exception("Failed to fetch book sources: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkError(e))
        }
    }

    suspend fun getRandomBooks(count: Int = 5): Result<BookResponse> {
        return try {
            val response = bookSourceApi.getRandomBooks(count)
            if (response.isSuccessful) {
                Result.success(response.body() ?: BookResponse())
            } else {
                Result.failure(Exception("Failed to fetch random books: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkError(e))
        }
    }

    private fun handleNetworkError(exception: Exception): Exception {
        return when (exception) {
            is java.net.UnknownHostException -> Exception("No internet connection")
            is java.net.SocketTimeoutException -> Exception("Request timeout")
            is retrofit2.HttpException -> {
                when (exception.code()) {
                    404 -> Exception("Resource not found")
                    500 -> Exception("Server error")
                    else -> Exception("HTTP error: ${exception.code()}")
                }
            }
            else -> Exception("Network error: ${exception.message}")
        }
    }
}