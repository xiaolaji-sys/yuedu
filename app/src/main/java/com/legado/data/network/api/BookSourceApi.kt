package com.legado.data.network.api

import com.legado.data.network.model.BookResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BookSourceApi {

    @GET("books")
    suspend fun getBooks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("source") source: String? = null
    ): Response<BookResponse>

    @GET("books/{id}")
    suspend fun getBookById(
        @Path("id") bookId: String
    ): Response<BookResponse>

    @GET("books/search")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Query("source") source: String? = null
    ): Response<BookResponse>

    @GET("sources")
    suspend fun getBookSources(): Response<List<String>>

    @GET("sources/{id}/books")
    suspend fun getBooksFromSource(
        @Path("id") sourceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<BookResponse>

    @GET("sources/discover")
    suspend fun discoverBooks(
        @Query("source") sourceIds: List<String>? = null,
        @Query("category") category: String? = null
    ): Response<BookResponse>

    @POST("sources")
    suspend fun addBookSource(@Body source: com.legado.data.network.model.BookSource): Response<Void>

    @PUT("sources/{id}")
    suspend fun updateBookSource(
        @Path("id") sourceId: String,
        @Body source: com.legado.data.network.model.BookSource
    ): Response<Void>

    @DELETE("sources/{id}")
    suspend fun deleteBookSource(@Path("id") sourceId: String): Response<Void>

    @GET("sources/{id}/search")
    suspend fun searchInSource(
        @Path("id") sourceId: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): Response<BookResponse>

    @GET("books/random")
    suspend fun getRandomBooks(
        @Query("count") count: Int = 5
    ): Response<BookResponse>
}