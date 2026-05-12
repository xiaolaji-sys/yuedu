package com.legado.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Book entity representing a book in the database
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "book_id")
    val bookId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "cover_url")
    val coverUrl: String?,

    @ColumnInfo(name = "intro")
    val intro: String?,

    @ColumnInfo(name = "category")
    val category: String?,

    @ColumnInfo(name = "last_check_time")
    val lastCheckTime: Long,

    @ColumnInfo(name = "update_time")
    val updateTime: Long,

    @ColumnInfo(name = "create_time")
    val createTime: Long,

    @ColumnInfo(name = "word_count")
    val wordCount: Int?,

    @ColumnInfo(name = "chapters_count")
    val chaptersCount: Int,

    @ColumnInfo(name = "to_chapter_position")
    val toChapterPosition: Int,

    @ColumnInfo(name = "current_chapter_position")
    val currentChapterPosition: Int,

    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "source_id")
    val sourceId: String,

    @ColumnInfo(name = "origin_name")
    val originName: String,

    @ColumnInfo(name = "custom_cover")
    val customCover: String?
)