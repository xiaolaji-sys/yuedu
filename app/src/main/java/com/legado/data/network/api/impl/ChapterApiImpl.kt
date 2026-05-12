package com.legado.data.network.api.impl

import com.legado.data.network.api.ChapterApi
import com.legado.data.network.model.BookResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterApiImpl @Inject constructor(
    private val chapterApi: ChapterApi
) : ChapterApi {

    override suspend fun getBookChapters(bookId: String): Response<BookResponse> {
        return chapterApi.getBookChapters(bookId)
    }

    override suspend fun getChapterById(chapterId: String): Response<BookResponse> {
        return chapterApi.getChapterById(chapterId)
    }

    override suspend fun getChapterByOrder(bookId: String, chapterOrder: Int): Response<BookResponse> {
        return chapterApi.getChapterByOrder(bookId, chapterOrder)
    }
}