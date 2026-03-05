package com.example.craftnook.data.repository

import com.example.craftnook.data.database.ArtMaterialDao
import kotlinx.coroutines.flow.Flow

class RoomRepository(
    private val artMaterialDao: ArtMaterialDao
) : IArtMaterialRepository {

    init {
        initializeSampleDataIfNeeded()
    }

    private fun initializeSampleDataIfNeeded() {
    }

    override fun getAllMaterials(): Flow<List<ArtMaterial>> {
        return artMaterialDao.getAllMaterials()
    }

    override suspend fun updateQuantity(materialId: String, newQuantity: Int): Result<Unit> {
        return try {
            if (newQuantity < 0) {
                return Result.failure(IllegalArgumentException("Quantity cannot be negative"))
            }

            artMaterialDao.updateQuantity(materialId, newQuantity, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMaterialById(materialId: String): Flow<ArtMaterial?> {
        return artMaterialDao.getMaterialById(materialId)
    }

    override suspend fun addMaterial(name: String, brand: String, quantity: Int, category: String, unit: String, imageUri: String?): Result<ArtMaterial> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be empty"))
            }
            if (quantity <= 0) {
                return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
            }

            val newId = System.currentTimeMillis().toString()

             val newMaterial = ArtMaterial(
                 id = newId,
                 name = name,
                 description = brand.ifBlank { "No brand specified" },
                 category = category,
                 quantity = quantity,
                 unit = unit.ifBlank { "unit" },
                 photoUri = imageUri
             )

            artMaterialDao.insertMaterial(newMaterial)
            Result.success(newMaterial)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMaterial(materialId: String): Result<Unit> {
        return try {
            artMaterialDao.deleteMaterial(materialId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMaterial(material: ArtMaterial): Result<Unit> {
        return try {
            artMaterialDao.updateMaterial(material.copy(lastUpdated = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
