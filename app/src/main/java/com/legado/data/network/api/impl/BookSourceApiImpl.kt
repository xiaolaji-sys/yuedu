package com.legado.data.network.api.impl

import com.legado.data.network.api.BookSourceApi
import com.legado.data.network.model.BookResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookSourceApiImpl @Inject constructor(
    private val bookSourceApi: BookSourceApi
) : BookSourceApi {

    override suspend fun getBooks(
        page: Int,
        limit: Int,
        source: String?
    ): Response<BookResponse> {
        return bookSourceApi.getBooks(page, limit, source)
    }

    override suspend fun getBookById(bookId: String): Response<BookResponse> {
        return bookSourceApi.getBookById(bookId)
    }

    override suspend fun searchBooks(query: String, source: String?): Response<BookResponse> {
        return bookSourceApi.searchBooks(query, source)
    }

    override suspend fun getBookSources(): Response<List<String>> {
        return bookSourceApi.getBookSources()
    }

    override suspend fun getRandomBooks(count: Int): Response<BookResponse> {
        return bookSourceApi.getRandomBooks(count)
    }
}