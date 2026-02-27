package com.example.modernandroidapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernandroidapp.data.repository.ArtMaterial
import com.example.modernandroidapp.data.repository.IArtMaterialRepository
import com.example.modernandroidapp.data.repository.InMemoryArtMaterialRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for inventory management
 * Handles state management and business logic for the inventory screen
 */
class InventoryViewModel(
    private val materialRepository: IArtMaterialRepository = InMemoryArtMaterialRepository()
) : ViewModel() {

    companion object {
        /**
         * Fixed list of available categories
         * Users can only assign materials to these categories
         */
        val FIXED_CATEGORIES = listOf(
            "Paint",
            "Brushes",
            "Canvas",
            "Paper",
            "Pencils",
            "Markers",
            "Sketchbooks",
            "Other"
        )
    }

    /**
     * StateFlow containing all art materials
     * Updated reactively when repository data changes
     */
    val allMaterials: StateFlow<List<ArtMaterial>> = materialRepository.getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Search query state
     * Updates in real-time as user types
     */
    private val _searchQuery = kotlinx.coroutines.flow.MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Selected category filter
     * "All" means no category filtering
     */
    private val _selectedCategory = kotlinx.coroutines.flow.MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    /**
     * All available categories extracted from materials
     * Used to populate filter chip options
     */
    val availableCategories: StateFlow<List<String>> = allMaterials
        .combine(_searchQuery) { _, _ ->
            FIXED_CATEGORIES
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = FIXED_CATEGORIES
        )

    /**
     * Filtered materials based on search query and selected category
     * Updates instantly as user types or selects a category
     */
    val filteredMaterials: StateFlow<List<ArtMaterial>> = allMaterials
        .combine(_searchQuery) { materials, query ->
            materials.filter { material ->
                material.name.contains(query, ignoreCase = true) ||
                material.description.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedCategory) { searchResults, category ->
            if (category == "All") {
                searchResults
            } else {
                searchResults.filter { it.category == category }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Loading state indicator
     * Can be used for showing/hiding loading indicators
     */
     private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
     val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Error message state
     * Set when operations fail
     */
    private val _errorMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Update the quantity of a material
     * Triggers a coroutine to update the repository
     *
     * @param materialId ID of the material to update
     * @param newQuantity New quantity value
     */
    fun updateMaterialQuantity(materialId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = materialRepository.updateQuantity(materialId, newQuantity)
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update quantity"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Update the search query
     * Filters materials by name and description in real-time
     *
     * @param query Search query string
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Select a category filter
     * Updates the filtered materials list instantly
     *
     * @param category Category name or "All" for no filter
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Add a new material to the inventory
     * Triggers a coroutine to add the material to the repository
     *
     * @param name Name of the material
     * @param brand Brand or manufacturer name
     * @param quantity Initial quantity
     * @param category Category of the material
     * @return Result with the created ArtMaterial on success, Exception on failure
     */
    fun addMaterial(name: String, brand: String, quantity: Int, category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = materialRepository.addMaterial(name, brand, quantity, category)
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to add material"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a material from the inventory
     * Triggers a coroutine to delete the material from the repository
     *
     * @param materialId ID of the material to delete
     */
    fun deleteMaterial(materialId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = materialRepository.deleteMaterial(materialId)
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete material"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing material in the inventory
     * Triggers a coroutine to update the material in the repository
     *
     * @param material The updated material
     */
    fun updateMaterial(material: ArtMaterial) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = materialRepository.updateMaterial(material)
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update material"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
