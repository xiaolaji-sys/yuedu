package com.legado.data.network.model

import com.google.gson.annotations.SerializedName

data class BookResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<Book>? = emptyList(),

    @SerializedName("total")
    val total: Int? = null,

    @SerializedName("page")
    val page: Int? = null,

    @SerializedName("limit")
    val limit: Int? = null
)

data class Book(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("author")
    val author: String? = null,

    @SerializedName("cover")
    val cover: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("lastChapter")
    val lastChapter: String? = null,

    @SerializedName("categories")
    val categories: List<String> = emptyList(),

    @SerializedName("chapters")
    val chapters: List<Chapter>? = null,

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("wordCount")
    val wordCount: Long? = null,

    @SerializedName("updateTime")
    val updateTime: Long? = null,

    @SerializedName("createdAt")
    val createdAt: Long? = null
)

data class Chapter(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("chapterOrder")
    val chapterOrder: Int? = null,

    @SerializedName("isVip")
    val isVip: Boolean = false,

    @SerializedName("isNew")
    val isNew: Boolean = false
)