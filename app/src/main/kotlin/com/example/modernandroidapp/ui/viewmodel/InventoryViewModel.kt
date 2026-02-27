package com.example.modernandroidapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernandroidapp.data.repository.ArtMaterial
import com.example.modernandroidapp.data.repository.IArtMaterialRepository
import com.example.modernandroidapp.data.repository.InMemoryArtMaterialRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for inventory management
 * Handles state management and business logic for the inventory screen
 */
class InventoryViewModel(
    private val materialRepository: IArtMaterialRepository = InMemoryArtMaterialRepository()
) : ViewModel() {

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
     * StateFlow containing only low stock materials
     * Emits materials where quantity <= minStock
     */
    val lowStockMaterials: StateFlow<List<ArtMaterial>> = materialRepository.getLowStockMaterials()
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
     * Check if a material is low on stock
     *
     * @param material The material to check
     * @return true if quantity <= minStock, false otherwise
     */
    fun isLowStock(material: ArtMaterial): Boolean {
        return material.quantity <= material.minStock
    }

    /**
     * Add a new material to the inventory
     * Triggers a coroutine to add the material to the repository
     *
     * @param name Name of the material
     * @param brand Brand or manufacturer name
     * @param quantity Initial quantity
     * @return Result with the created ArtMaterial on success, Exception on failure
     */
    fun addMaterial(name: String, brand: String, quantity: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = materialRepository.addMaterial(name, brand, quantity)
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
