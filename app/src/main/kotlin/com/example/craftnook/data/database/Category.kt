package com.example.craftnook.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ── Entity ───────────────────────────────────────────────────────────────────

/**
 * A user-defined craft material category stored in Room.
 *
 * The [name] is the primary key so duplicates are naturally rejected.
 * Categories seeded from the original hardcoded list arrive with
 * [isDefault] = true so the UI can optionally distinguish them.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String,
    val isDefault: Boolean = false
)

// ── DAO ──────────────────────────────────────────────────────────────────────

@Dao
interface CategoryDao {

    /** Emits the full list ordered A→Z whenever it changes. */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Upsert a category. Using IGNORE so that seeding the default set
     * during migration does not overwrite any user edits.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    /** Bulk insert used during the migration seed. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
