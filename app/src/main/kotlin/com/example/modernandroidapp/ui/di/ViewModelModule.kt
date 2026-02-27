package com.example.modernandroidapp.ui.di

import androidx.lifecycle.ViewModel
import com.example.modernandroidapp.ui.viewmodel.InventoryViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * Hilt module for ViewModel dependency injection.
 *
 * Provides ViewModels as singletons within the application.
 * Uses Hilt's ViewModelComponent for proper lifecycle management.
 *
 * New ViewModels should be added to this module as they are created.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelModule {
    /**
     * Provides the InventoryViewModel singleton.
     *
     * The InventoryViewModel manages the state of the inventory list screen
     * and handles communication with the repository layer.
     *
     * @param viewModel The InventoryViewModel instance to provide
     * @return The InventoryViewModel
     */
    @Binds
    @IntoMap
    @StringKey("InventoryViewModel")
    abstract fun bindInventoryViewModel(viewModel: InventoryViewModel): ViewModel
}
