package com.legado.data.network.model

import com.google.gson.annotations.SerializedName

data class BookSourceModel(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("baseUrl")
    val baseUrl: String,

    @SerializedName("enabled")
    val enabled: Boolean = true,

    @SerializedName("lastUpdate")
    val lastUpdate: Long = 0L,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("rules")
    val rules: List<Rule> = emptyList()
)

data class Rule(
    @SerializedName("selector")
    val selector: String,

    @SerializedName("attribute")
    val attribute: String? = null,

    @SerializedName("regex")
    val regex: String? = null,

    @SerializedName("replacement")
    val replacement: String? = null
)