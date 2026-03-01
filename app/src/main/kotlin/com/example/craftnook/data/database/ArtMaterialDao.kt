package com.example.craftnook.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.craftnook.data.repository.ArtMaterial
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for ArtMaterial entities
 * Defines all database operations for art materials
 */
@Dao
interface ArtMaterialDao {

    /**
     * Get all art materials ordered by name
     * @return Flow of list of all materials
     */
    @Query("SELECT * FROM art_materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<ArtMaterial>>

    /**
     * Get a specific material by ID
     * @param materialId ID of the material
     * @return Flow of the material or null if not found
     */
    @Query("SELECT * FROM art_materials WHERE id = :materialId")
    fun getMaterialById(materialId: String): Flow<ArtMaterial?>

    /**
     * Insert a new material into the database
     * If material with same ID exists, it will be replaced
     * @param material The material to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: ArtMaterial)

    /**
     * Insert a material only if no row with the same ID already exists.
     * Used during backup import to avoid overwriting user data with older backup data.
     * Returns the new rowId, or -1 if the row was skipped due to conflict.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaterialIfNotExists(material: ArtMaterial): Long

    /**
     * Update an existing material
     * @param material The material to update
     */
    @Update
    suspend fun updateMaterial(material: ArtMaterial)

    /**
     * Delete a material by ID
     * @param material The material to delete
     */
    @Delete
    suspend fun deleteMaterial(material: ArtMaterial)

    /**
     * Delete a material by its ID
     * @param materialId ID of the material to delete
     */
    @Query("DELETE FROM art_materials WHERE id = :materialId")
    suspend fun deleteMaterial(materialId: String)

    /**
     * Update the quantity of a material
     * @param materialId ID of the material to update
     * @param newQuantity New quantity value
     * @param lastUpdated Current timestamp
     */
    @Query("UPDATE art_materials SET quantity = :newQuantity, lastUpdated = :lastUpdated WHERE id = :materialId")
    suspend fun updateQuantity(materialId: String, newQuantity: Int, lastUpdated: Long)

    /**
     * Get count of all materials
     * @return Count of materials
     */
    @Query("SELECT COUNT(*) FROM art_materials")
    suspend fun getMaterialCount(): Int
}
