package com.example.craftnook.ui.di

import android.content.Context
import androidx.room.Room
import com.example.craftnook.data.database.AppDatabase
import com.example.craftnook.data.database.ArtMaterialDao
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
 * Data persists even after the app is closed.
 */
@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    /**
     * Provides a singleton instance of the Room AppDatabase.
     *
     * Creates the database with the specified name and persists data to device storage.
     * All queries are executed on a background thread automatically.
     *
     * @param context Application context for database creation
     * @return The Room AppDatabase instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME
        ).build()
    }

    /**
     * Provides the ArtMaterialDao from the database.
     *
     * @param appDatabase The Room AppDatabase instance
     * @return The ArtMaterialDao for database operations
     */
    @Provides
    @Singleton
    fun provideArtMaterialDao(appDatabase: AppDatabase): ArtMaterialDao {
        return appDatabase.artMaterialDao()
    }

}
