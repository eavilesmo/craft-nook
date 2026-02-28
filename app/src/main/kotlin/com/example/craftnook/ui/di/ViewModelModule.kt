package com.example.craftnook.ui.di

import androidx.lifecycle.ViewModel
import com.example.craftnook.data.repository.IArtMaterialRepository
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

/**
 * Hilt module for ViewModel dependency injection.
 *
 * Provides ViewModels as singletons within the application.
 * Uses Hilt's SingletonComponent for proper lifecycle management.
 *
 * New ViewModels should be added to this module as they are created.
 */
@Module
@InstallIn(SingletonComponent::class)
class ViewModelModule {
    /**
     * Provides the InventoryViewModel singleton.
     *
     * The InventoryViewModel manages the state of the inventory list screen
     * and handles communication with the repository layer.
     * @param materialRepository The art material repository
     * @return The InventoryViewModel instance
     */
    @Provides
    @Singleton
    fun provideInventoryViewModel(
        materialRepository: IArtMaterialRepository
    ): InventoryViewModel {
        return InventoryViewModel(materialRepository)
    }
}
