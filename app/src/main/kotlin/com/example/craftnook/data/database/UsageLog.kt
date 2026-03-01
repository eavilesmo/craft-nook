package com.example.craftnook.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

// ── Event type constants ─────────────────────────────────────────────────────

object EventType {
    const val ADDED      = "ADDED"
    const val RESTOCKED  = "RESTOCKED"
    const val USED       = "USED"
    const val DELETED    = "DELETED"
}

// ── Entity ───────────────────────────────────────────────────────────────────

/**
 * Room entity representing a single journal event.
 *
 * [materialName] and [category] are intentionally denormalised: they are
 * copied from the material at the time of the event so that log entries
 * remain readable after a material is renamed or deleted.
 */
@Serializable
@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey val id: String,
    val materialId: String,
    /** Snapshot of the material name at the time of the event. */
    val materialName: String,
    /** Snapshot of the category at the time of the event. */
    val category: String,
    /** One of [EventType]. */
    val eventType: String,
    /** Positive = gained, negative = consumed/deleted. */
    val quantityDelta: Int,
    /** Quantity after the event. */
    val quantityAfter: Int,
    /** Epoch milliseconds. */
    val timestamp: Long = System.currentTimeMillis()
)

// ── DAO ──────────────────────────────────────────────────────────────────────

@Dao
interface UsageLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: UsageLog)

    /**
     * Insert a log entry only if no row with the same ID already exists.
     * Used during backup import to avoid duplicate journal entries.
     * Returns the new rowId, or -1 if the row was skipped due to conflict.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(log: UsageLog): Long

    /** All entries, newest first. */
    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<UsageLog>>

    /** Entries filtered by event type, newest first. */
    @Query("SELECT * FROM usage_logs WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getLogsByType(eventType: String): Flow<List<UsageLog>>

    /** All entries for a specific material, newest first. */
    @Query("SELECT * FROM usage_logs WHERE materialId = :materialId ORDER BY timestamp DESC")
    fun getLogsByMaterial(materialId: String): Flow<List<UsageLog>>

    @Query("DELETE FROM usage_logs")
    suspend fun deleteAll()
}
