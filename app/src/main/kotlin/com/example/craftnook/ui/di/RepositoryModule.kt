package com.example.craftnook.ui.di

import android.content.Context
import com.example.craftnook.data.database.ArtMaterialDao
import com.example.craftnook.data.database.CategoryDao
import com.example.craftnook.data.database.UsageLogDao
import com.example.craftnook.data.repository.IArtMaterialRepository
import com.example.craftnook.data.repository.IBackupRepository
import com.example.craftnook.data.repository.ICategoryRepository
import com.example.craftnook.data.repository.IUsageLogRepository
import com.example.craftnook.data.repository.RoomBackupRepository
import com.example.craftnook.data.repository.RoomCategoryRepository
import com.example.craftnook.data.repository.RoomRepository
import com.example.craftnook.data.repository.RoomUsageLogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): ICategoryRepository {
        return RoomCategoryRepository(categoryDao)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        artMaterialDao: ArtMaterialDao,
        usageLogDao: UsageLogDao
    ): IBackupRepository {
        return RoomBackupRepository(context, artMaterialDao, usageLogDao)
    }
}
