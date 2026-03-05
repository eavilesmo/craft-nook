package com.example.craftnook.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "art_materials")
data class ArtMaterial(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val unit: String,
    val photoUri: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

interface IArtMaterialRepository {
    fun getAllMaterials(): Flow<List<ArtMaterial>>

    suspend fun updateQuantity(materialId: String, newQuantity: Int): Result<Unit>

    fun getMaterialById(materialId: String): Flow<ArtMaterial?>

    suspend fun addMaterial(name: String, brand: String, quantity: Int, category: String = "Other", unit: String = "unit", imageUri: String? = null): Result<ArtMaterial>

    suspend fun deleteMaterial(materialId: String): Result<Unit>

    suspend fun updateMaterial(material: ArtMaterial): Result<Unit>
}