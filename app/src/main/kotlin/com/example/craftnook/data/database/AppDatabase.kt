package com.example.craftnook.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.craftnook.data.repository.ArtMaterial

@Database(
    entities = [ArtMaterial::class, UsageLog::class, Category::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun artMaterialDao(): ArtMaterialDao

    abstract fun usageLogDao(): UsageLogDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "craft_nook_database"
    }
}
