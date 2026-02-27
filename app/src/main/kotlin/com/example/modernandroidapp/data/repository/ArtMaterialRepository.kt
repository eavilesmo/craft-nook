package com.example.modernandroidapp.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * ArtMaterial entity for Room Database
 * Represents an art material in the inventory with all its details
 */
@Entity(tableName = "art_materials")
data class ArtMaterial(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val price: Double,
    val minStock: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Repository interface for art material operations
 * Defines contracts for data access operations
 */
interface IArtMaterialRepository {
    /**
     * Get all available art materials
     * @return Flow of list of all materials
     */
    fun getAllMaterials(): Flow<List<ArtMaterial>>

    /**
     * Get materials with low stock (quantity <= minStock)
     * @return Flow of list of low stock materials
     */
    fun getLowStockMaterials(): Flow<List<ArtMaterial>>

    /**
     * Update the quantity of a material
     * @param materialId ID of the material to update
     * @param newQuantity New quantity value
     * @return Flow indicating success/failure
     */
    suspend fun updateQuantity(materialId: String, newQuantity: Int): Result<Unit>

    /**
     * Get a specific material by ID
     * @param materialId ID of the material
     * @return Flow of the material or null if not found
     */
    fun getMaterialById(materialId: String): Flow<ArtMaterial?>

    /**
     * Add a new material to the inventory
     * @param name Name of the material
     * @param brand Brand or manufacturer name
     * @param quantity Initial quantity
     * @return Result with the created ArtMaterial on success, Exception on failure
     */
    suspend fun addMaterial(name: String, brand: String, quantity: Int): Result<ArtMaterial>

    /**
     * Delete a material from the inventory
     * @param materialId ID of the material to delete
     * @return Result with Unit on success, Exception on failure
     */
    suspend fun deleteMaterial(materialId: String): Result<Unit>

    /**
     * Update an existing material
     * @param material The updated material
     * @return Result with Unit on success, Exception on failure
     */
    suspend fun updateMaterial(material: ArtMaterial): Result<Unit>
}

/**
 * In-Memory implementation of ArtMaterialRepository
 * Uses MutableStateFlow for reactive updates
 * Suitable for testing UI without Room database setup
 */
class InMemoryArtMaterialRepository : IArtMaterialRepository {

    private val _materials = MutableStateFlow<List<ArtMaterial>>(
        listOf(
            ArtMaterial(
                id = "1",
                name = "Acrylic Paint - Red",
                description = "Bright red acrylic paint",
                category = "Paint",
                quantity = 5,
                unit = "bottle",
                price = 12.99,
                minStock = 10
            ),
            ArtMaterial(
                id = "2",
                name = "Canvas - 8x10",
                description = "Blank canvas 8x10 inches",
                category = "Canvas",
                quantity = 3,
                unit = "piece",
                price = 8.50,
                minStock = 5
            ),
            ArtMaterial(
                id = "3",
                name = "Brush Set - Professional",
                description = "Set of 12 professional brushes",
                category = "Brushes",
                quantity = 2,
                unit = "set",
                price = 24.99,
                minStock = 3
            ),
            ArtMaterial(
                id = "4",
                name = "Watercolor Set",
                description = "24-color watercolor palette",
                category = "Paint",
                quantity = 7,
                unit = "palette",
                price = 18.75,
                minStock = 5
            ),
            ArtMaterial(
                id = "5",
                name = "Sketchbook - A4",
                description = "A4 blank sketchbook 100 pages",
                category = "Paper",
                quantity = 15,
                unit = "book",
                price = 9.99,
                minStock = 8
            ),
            ArtMaterial(
                id = "6",
                name = "Oil Paint - Blue",
                description = "Professional grade oil paint",
                category = "Paint",
                quantity = 2,
                unit = "tube",
                price = 15.50,
                minStock = 4
            )
        )
    )

    private val materials = _materials.asStateFlow()

    /**
     * Get all materials as a reactive Flow
     * Emits new list whenever materials are updated
     */
    override fun getAllMaterials(): Flow<List<ArtMaterial>> {
        return materials
    }

    /**
     * Get materials with low stock as a reactive Flow
     * Filters materials where quantity <= minStock
     */
    override fun getLowStockMaterials(): Flow<List<ArtMaterial>> {
        return materials.map { allMaterials ->
            allMaterials.filter { material ->
                material.quantity <= material.minStock
            }
        }
    }

    /**
     * Update quantity of a material by ID
     * Updates the in-memory state and returns success/failure result
     *
     * @param materialId ID of material to update
     * @param newQuantity New quantity value (must be >= 0)
     * @return Result with Unit on success, Exception on failure
     */
    override suspend fun updateQuantity(materialId: String, newQuantity: Int): Result<Unit> {
        return try {
            // Validate input
            if (newQuantity < 0) {
                return Result.failure(IllegalArgumentException("Quantity cannot be negative"))
            }

            // Find and update the material
            val updatedMaterials = _materials.value.map { material ->
                if (material.id == materialId) {
                    material.copy(
                        quantity = newQuantity,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    material
                }
            }

            // Check if material was found
            val materialFound = _materials.value.any { it.id == materialId }
            if (!materialFound) {
                return Result.failure(NoSuchElementException("Material with ID $materialId not found"))
            }

            // Update the state
            _materials.value = updatedMaterials
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a specific material by ID as a reactive Flow
     * Returns null if material not found
     */
    override fun getMaterialById(materialId: String): Flow<ArtMaterial?> {
        return materials.map { allMaterials ->
            allMaterials.firstOrNull { it.id == materialId }
        }
    }

    /**
     * Add a new material to the inventory
     * Generates a unique ID and adds the material to the in-memory store
     *
     * @param name Name of the material
     * @param brand Brand or manufacturer name (used as description)
     * @param quantity Initial quantity (must be > 0)
     * @return Result with the created ArtMaterial on success, Exception on failure
     */
    override suspend fun addMaterial(name: String, brand: String, quantity: Int): Result<ArtMaterial> {
        return try {
            // Validate inputs
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be empty"))
            }
            if (quantity <= 0) {
                return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
            }

            // Generate unique ID
            val newId = (_materials.value.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0).plus(1).toString()

            // Create new material with sensible defaults
            val newMaterial = ArtMaterial(
                id = newId,
                name = name,
                description = brand.ifBlank { "No brand specified" },
                category = "Other", // Default category
                quantity = quantity,
                unit = "unit", // Default unit
                price = 0.0, // Default price (can be updated later)
                minStock = 5 // Default min stock threshold
            )

            // Add to the list
            _materials.value = _materials.value + newMaterial
            Result.success(newMaterial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to get current materials snapshot (for testing)
     */
    fun getCurrentMaterials(): List<ArtMaterial> {
        return _materials.value
    }

    /**
     * Helper function to reset materials to initial state (for testing)
     */
    fun resetMaterials() {
        _materials.value = listOf(
            ArtMaterial(
                id = "1",
                name = "Acrylic Paint - Red",
                description = "Bright red acrylic paint",
                category = "Paint",
                quantity = 5,
                unit = "bottle",
                price = 12.99,
                minStock = 10
            ),
            ArtMaterial(
                id = "2",
                name = "Canvas - 8x10",
                description = "Blank canvas 8x10 inches",
                category = "Canvas",
                quantity = 3,
                unit = "piece",
                price = 8.50,
                minStock = 5
            ),
            ArtMaterial(
                id = "3",
                name = "Brush Set - Professional",
                description = "Set of 12 professional brushes",
                category = "Brushes",
                quantity = 2,
                unit = "set",
                price = 24.99,
                minStock = 3
            ),
            ArtMaterial(
                id = "4",
                name = "Watercolor Set",
                description = "24-color watercolor palette",
                category = "Paint",
                quantity = 7,
                unit = "palette",
                price = 18.75,
                minStock = 5
            ),
            ArtMaterial(
                id = "5",
                name = "Sketchbook - A4",
                description = "A4 blank sketchbook 100 pages",
                category = "Paper",
                quantity = 15,
                unit = "book",
                price = 9.99,
                minStock = 8
            ),
            ArtMaterial(
                id = "6",
                name = "Oil Paint - Blue",
                description = "Professional grade oil paint",
                category = "Paint",
                quantity = 2,
                unit = "tube",
                price = 15.50,
                minStock = 4
            )
        )
    }

    /**
     * Delete a material from the inventory
     * Removes the material with the given ID from the in-memory store
     *
     * @param materialId ID of the material to delete
     * @return Result with Unit on success, Exception on failure
     */
    override suspend fun deleteMaterial(materialId: String): Result<Unit> {
        return try {
            // Check if material exists
            val materialExists = _materials.value.any { it.id == materialId }
            if (!materialExists) {
                return Result.failure(NoSuchElementException("Material with ID $materialId not found"))
            }

            // Remove the material from the list
            _materials.value = _materials.value.filterNot { it.id == materialId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing material in the inventory
     * Updates the in-memory state and returns success/failure result
     *
     * @param material The updated material
     * @return Result with Unit on success, Exception on failure
     */
    override suspend fun updateMaterial(material: ArtMaterial): Result<Unit> {
        return try {
            // Check if material exists
            val materialExists = _materials.value.any { it.id == material.id }
            if (!materialExists) {
                return Result.failure(NoSuchElementException("Material with ID ${material.id} not found"))
            }

            // Update the material in the list
            _materials.value = _materials.value.map { existing ->
                if (existing.id == material.id) {
                    material.copy(lastUpdated = System.currentTimeMillis())
                } else {
                    existing
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
