package com.example.craftnook.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.craftnook.data.repository.ArtMaterial

/**
 * Room Database for Craft Nook application.
 *
 * Version history:
 *  1 → 2  Added [ArtMaterial.photoUri] column (migration in DatabaseModule).
 *  2 → 3  Added [UsageLog] table (migration in DatabaseModule).
 */
@Database(
    entities = [ArtMaterial::class, UsageLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun artMaterialDao(): ArtMaterialDao

    abstract fun usageLogDao(): UsageLogDao

    companion object {
        const val DATABASE_NAME = "craft_nook_database"
    }
}
