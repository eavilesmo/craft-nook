package com.example.craftnook.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.craftnook.data.repository.ArtMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtMaterialDao {

    @Query("SELECT * FROM art_materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<ArtMaterial>>

    @Query("SELECT * FROM art_materials WHERE id = :materialId")
    fun getMaterialById(materialId: String): Flow<ArtMaterial?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: ArtMaterial)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaterialIfNotExists(material: ArtMaterial): Long

    @Update
    suspend fun updateMaterial(material: ArtMaterial)

    @Delete
    suspend fun deleteMaterial(material: ArtMaterial)

    @Query("DELETE FROM art_materials WHERE id = :materialId")
    suspend fun deleteMaterial(materialId: String)

    @Query("UPDATE art_materials SET quantity = :newQuantity, lastUpdated = :lastUpdated WHERE id = :materialId")
    suspend fun updateQuantity(materialId: String, newQuantity: Int, lastUpdated: Long)

    @Query("SELECT COUNT(*) FROM art_materials")
    suspend fun getMaterialCount(): Int
}
