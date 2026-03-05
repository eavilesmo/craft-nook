package com.example.craftnook.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

object EventType {
    const val ADDED      = "ADDED"
    const val RESTOCKED  = "RESTOCKED"
    const val USED       = "USED"
    const val DELETED    = "DELETED"
}

@Serializable
@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey val id: String,
    val materialId: String,
    val materialName: String,
    val category: String,
    val eventType: String,
    val quantityDelta: Int,
    val quantityAfter: Int,
    val timestamp: Long = System.currentTimeMillis()
)


@Dao
interface UsageLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: UsageLog)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(log: UsageLog): Long

    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<UsageLog>>

    @Query("SELECT * FROM usage_logs WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getLogsByType(eventType: String): Flow<List<UsageLog>>

    @Query("SELECT * FROM usage_logs WHERE materialId = :materialId ORDER BY timestamp DESC")
    fun getLogsByMaterial(materialId: String): Flow<List<UsageLog>>

    @Query("DELETE FROM usage_logs")
    suspend fun deleteAll()

    @Query("DELETE FROM usage_logs WHERE id = :id")
    suspend fun deleteById(id: String)
}
