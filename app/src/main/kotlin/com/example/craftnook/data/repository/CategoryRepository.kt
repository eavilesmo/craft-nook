package com.example.craftnook.data.repository

import com.example.craftnook.data.database.Category
import com.example.craftnook.data.database.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ── Interface ────────────────────────────────────────────────────────────────

interface ICategoryRepository {
    /** Emits the sorted list of category names whenever the table changes. */
    fun getAllCategoryNames(): Flow<List<String>>

    /** Add a new category. No-op if a category with this name already exists. */
    suspend fun addCategory(name: String)

    /** Remove a category by name. */
    suspend fun deleteCategory(name: String)
}

// ── Room implementation ──────────────────────────────────────────────────────

class RoomCategoryRepository(
    private val categoryDao: CategoryDao
) : ICategoryRepository {

    override fun getAllCategoryNames(): Flow<List<String>> =
        categoryDao.getAllCategories().map { list -> list.map { it.name } }

    override suspend fun addCategory(name: String) {
        categoryDao.insert(Category(name = name.trim(), isDefault = false))
    }

    override suspend fun deleteCategory(name: String) {
        categoryDao.delete(Category(name = name))
    }
}
