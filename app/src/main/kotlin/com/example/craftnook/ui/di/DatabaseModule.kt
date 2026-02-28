package com.example.craftnook.ui.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.craftnook.data.database.AppDatabase
import com.example.craftnook.data.database.ArtMaterialDao
import com.example.craftnook.data.database.UsageLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependency injection.
 *
 * Provides singleton instances of the Room database and DAOs.
 * The database is created once and reused throughout the application lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    /** v1 → v2: adds nullable photoUri column to art_materials. */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE art_materials ADD COLUMN photoUri TEXT DEFAULT NULL"
            )
        }
    }

    /** v2 → v3: creates the usage_logs table. */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS usage_logs (
                    id TEXT NOT NULL PRIMARY KEY,
                    materialId TEXT NOT NULL,
                    materialName TEXT NOT NULL,
                    category TEXT NOT NULL,
                    eventType TEXT NOT NULL,
                    quantityDelta INTEGER NOT NULL,
                    quantityAfter INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    @Singleton
    fun provideArtMaterialDao(appDatabase: AppDatabase): ArtMaterialDao {
        return appDatabase.artMaterialDao()
    }

    @Provides
    @Singleton
    fun provideUsageLogDao(appDatabase: AppDatabase): UsageLogDao {
        return appDatabase.usageLogDao()
    }
}
