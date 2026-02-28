package com.example.craftnook.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.craftnook.data.repository.ArtMaterial

/**
 * Room Database for Craft Nook application
 * Manages all database operations and entity relationships
 */
@Database(
    entities = [ArtMaterial::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Get the DAO for ArtMaterial operations
     */
    abstract fun artMaterialDao(): ArtMaterialDao

    companion object {
        const val DATABASE_NAME = "craft_nook_database"
    }
}
