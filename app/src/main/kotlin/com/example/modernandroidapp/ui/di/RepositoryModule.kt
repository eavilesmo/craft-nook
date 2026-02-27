package com.example.modernandroidapp.ui.di

import com.example.modernandroidapp.data.database.ArtMaterialDao
import com.example.modernandroidapp.data.repository.IArtMaterialRepository
import com.example.modernandroidapp.data.repository.RoomRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependency injection.
 *
 * Provides singleton instances of repository implementations.
 * Currently provides the Room-based repository for persistent data storage.
 *
 * The repository uses Room Database for all data operations, ensuring
 * data persists even after the app is closed.
 *
 * Future implementations:
 * - Add NetworkArtMaterialRepository for API integration
 * - Implement repository pattern selection based on configuration
 */
@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    /**
     * Provides a singleton instance of the ArtMaterialRepository.
     *
     * Uses RoomRepository for persistent storage with SQLite.
     * This can be easily swapped with Network or Hybrid implementations
     * without changing the rest of the application code.
     *
     * @param artMaterialDao The DAO for database operations
     * @return An implementation of IArtMaterialRepository
     */
    @Provides
    @Singleton
    fun provideArtMaterialRepository(artMaterialDao: ArtMaterialDao): IArtMaterialRepository {
        return RoomRepository(artMaterialDao)
    }
}

