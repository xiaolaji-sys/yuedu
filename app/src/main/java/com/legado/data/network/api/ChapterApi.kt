package com.legado.data.network.api

import com.legado.data.network.model.BookResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ChapterApi {

    @GET("books/{bookId}/chapters")
    suspend fun getBookChapters(
        @Path("bookId") bookId: String
    ): Response<BookResponse>

    @GET("chapters/{chapterId}")
    suspend fun getChapterById(
        @Path("chapterId") chapterId: String
    ): Response<BookResponse>

    @GET("books/{bookId}/chapters/{chapterOrder}")
    suspend fun getChapterByOrder(
        @Path("bookId") bookId: String,
        @Path("chapterOrder") chapterOrder: Int
    ): Response<BookResponse>
}