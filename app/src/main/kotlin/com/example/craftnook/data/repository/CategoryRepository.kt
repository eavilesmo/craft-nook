package com.example.craftnook.data.repository

import com.example.craftnook.data.database.Category
import com.example.craftnook.data.database.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


interface ICategoryRepository {
    fun getAllCategoryNames(): Flow<List<String>>

    suspend fun addCategory(name: String)

    suspend fun deleteCategory(name: String)
}

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
