package com.example.craftnook.ui.di

import com.example.craftnook.data.database.ArtMaterialDao
import com.example.craftnook.data.database.UsageLogDao
import com.example.craftnook.data.repository.IArtMaterialRepository
import com.example.craftnook.data.repository.IUsageLogRepository
import com.example.craftnook.data.repository.RoomRepository
import com.example.craftnook.data.repository.RoomUsageLogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideArtMaterialRepository(artMaterialDao: ArtMaterialDao): IArtMaterialRepository {
        return RoomRepository(artMaterialDao)
    }

    @Provides
    @Singleton
    fun provideUsageLogRepository(usageLogDao: UsageLogDao): IUsageLogRepository {
        return RoomUsageLogRepository(usageLogDao)
    }
}
