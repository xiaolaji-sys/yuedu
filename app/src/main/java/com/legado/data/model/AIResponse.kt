package com.legado.data.model

import com.google.gson.annotations.SerializedName

data class AIResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AIDirectoryData? = null
)

data class AIDirectoryData(
    @SerializedName("chapters")
    val chapters: List<Chapter>? = null,

    @SerializedName("summary")
    val summary: String? = null,

    @SerializedName("totalChapters")
    val totalChapters: Int? = null,

    @SerializedName("estimatedReadingTime")
    val estimatedReadingTime: Int? = null,

    @SerializedName("structureAnalysis")
    val structureAnalysis: String? = null
)

data class Chapter(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("chapterNumber")
    val chapterNumber: Int? = null,

    @SerializedName("estimatedWords")
    val estimatedWords: Int? = null,

    @SerializedName("difficulty")
    val difficulty: String? = "medium",

    @SerializedName("keywords")
    val keywords: List<String> = emptyList(),

    @SerializedName("summary")
    val summary: String? = null,

    @SerializedName("parentSection")
    val parentSection: String? = null
)

enum class ChapterDifficulty(val displayName: String) {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难"),
    ADVANCED("进阶")
}

data class AIGenerationRequest(
    @SerializedName("bookTitle")
    val bookTitle: String,

    @SerializedName("bookDescription")
    val bookDescription: String? = null,

    @SerializedName("genre")
    val genre: String? = null,

    @SerializedName("targetAudience")
    val targetAudience: String? = null,

    @SerializedName("style")
    val style: String? = "standard",

    @SerializedName("complexity")
    val complexity: String? = "medium"
)