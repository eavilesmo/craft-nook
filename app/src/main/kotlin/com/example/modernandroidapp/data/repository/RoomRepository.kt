package com.example.modernandroidapp.data.repository

import com.example.modernandroidapp.data.database.ArtMaterialDao
import kotlinx.coroutines.flow.Flow

/**
 * Room Database implementation of ArtMaterialRepository
 * Provides persistent data storage using Room with SQLite
 * All data is automatically persisted and survives app restarts
 *
 * @param artMaterialDao The DAO for art material database operations
 */
class RoomRepository(
    private val artMaterialDao: ArtMaterialDao
) : IArtMaterialRepository {

    init {
        // Initialize sample data on first launch if database is empty
        initializeSampleDataIfNeeded()
    }

    /**
     * Initialize the database with sample data on first launch
     * This is called once when the repository is created
     */
    private fun initializeSampleDataIfNeeded() {
        // Database starts empty - users add materials through the UI
        // Sample data comment preserved for future use:
        // val sampleMaterials = listOf(
        //     ArtMaterial(...), ...
        // )
    }

    /**
     * Get all materials as a reactive Flow
     * Automatically emits updates when data changes in the database
     * @return Flow of list of all materials
     */
    override fun getAllMaterials(): Flow<List<ArtMaterial>> {
        return artMaterialDao.getAllMaterials()
    }

    /**
     * Update quantity of a material by ID
     * Updates the in-database state and returns success/failure result
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

            // Update in database
            artMaterialDao.updateQuantity(materialId, newQuantity, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a specific material by ID as a reactive Flow
     * Returns null if material not found
     * @param materialId ID of the material
     * @return Flow of the material or null if not found
     */
    override fun getMaterialById(materialId: String): Flow<ArtMaterial?> {
        return artMaterialDao.getMaterialById(materialId)
    }

    /**
     * Add a new material to the inventory
     * Persists the material to the database
     *
     * @param name Name of the material
     * @param brand Brand or manufacturer name (used as description)
     * @param quantity Initial quantity (must be > 0)
     * @param category Category of the material
     * @return Result with the created ArtMaterial on success, Exception on failure
     */
    override suspend fun addMaterial(name: String, brand: String, quantity: Int, category: String): Result<ArtMaterial> {
        return try {
            // Validate inputs
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be empty"))
            }
            if (quantity <= 0) {
                return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
            }

            // Generate unique ID (using current timestamp with random suffix)
            val newId = System.currentTimeMillis().toString()

            // Create new material with sensible defaults
            val newMaterial = ArtMaterial(
                id = newId,
                name = name,
                description = brand.ifBlank { "No brand specified" },
                category = category,
                quantity = quantity,
                unit = "unit", // Default unit
                price = 0.0 // Default price (can be updated later)
            )

            // Insert into database
            artMaterialDao.insertMaterial(newMaterial)
            Result.success(newMaterial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a material from the inventory
     * Removes the material with the given ID from the database
     *
     * @param materialId ID of the material to delete
     * @return Result with Unit on success, Exception on failure
     */
    override suspend fun deleteMaterial(materialId: String): Result<Unit> {
        return try {
            // Delete from database
            artMaterialDao.deleteMaterial(materialId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing material in the inventory
     * Updates the material in the database
     *
     * @param material The updated material
     * @return Result with Unit on success, Exception on failure
     */
    override suspend fun updateMaterial(material: ArtMaterial): Result<Unit> {
        return try {
            // Update in database
            artMaterialDao.updateMaterial(material.copy(lastUpdated = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

