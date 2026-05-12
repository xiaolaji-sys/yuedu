package com.legado.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.legado.data.database.dao.BookDao
import com.legado.data.database.entities.BookEntity

/**
 * Main Room database for the Legado application
 */
@Database(
    entities = [BookEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LegadoDatabase : RoomDatabase() {

    /**
     * Get the Book DAO for database operations
     */
    abstract fun bookDao(): BookDao

    companion object {
        // Singleton instance to prevent multiple instances of the database
        @Volatile
        private var INSTANCE: LegadoDatabase? = null

        /**
         * Get the singleton instance of the database
         */
        fun getDatabase(context: Context): LegadoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LegadoDatabase::class.java,
                    "legado_database"
                )
                    .fallbackToDestructiveMigration() // For now, will improve with proper migration strategy
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Get the current database instance (must be called after initialization)
         */
        fun getInstance(): LegadoDatabase? {
            return INSTANCE
        }
    }
}

/**
 * Type converters for complex data types
 */
class Converters {
    // Add converters here if needed in the future
    // For now, basic types are handled automatically by Room
}