package com.example.craftnook.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craftnook.data.database.EventType
import com.example.craftnook.data.database.UsageLog
import com.example.craftnook.data.repository.ArtMaterial
import com.example.craftnook.data.repository.IArtMaterialRepository
import com.example.craftnook.data.repository.IBackupRepository
import com.example.craftnook.data.repository.ICategoryRepository
import com.example.craftnook.data.repository.IUsageLogRepository
import com.example.craftnook.data.util.copyImageToInternalStorage
import com.example.craftnook.data.util.deleteInternalImage
import com.example.craftnook.data.util.isInternalImagePath
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import androidx.core.net.toUri

enum class StockFilter { ALL, IN_STOCK, FINISHED }

data class CategoryStat(
    val category: String,
    val units: Int,
    val percentage: Float,
    val unitLabel: String = "units"
)

sealed class BackupResult {
    object ExportSuccess : BackupResult()
    data class ImportSuccess(val materialsAdded: Int, val logsAdded: Int) : BackupResult()
    data class Failure(val message: String) : BackupResult()
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val materialRepository: IArtMaterialRepository,
    private val logRepository: IUsageLogRepository,
    private val categoryRepository: ICategoryRepository,
    private val backupRepository: IBackupRepository
) : ViewModel() {

    companion object {
        val FIXED_CATEGORIES = listOf(
            "A4 Notebooks",
            "A5 Notebooks",
            "Alcohol Markers",
            "Brushes",
            "Colored Pencils",
            "Crayons",
            "Drawing Pencils",
            "Erasers",
            "Fineliners",
            "Fountain Pen Cartridges",
            "Glitter Pens",
            "Highlighters",
            "Mechanical Pencil Leads",
            "Mechanical Pencils",
            "Metallic Pens",
            "Paint",
            "Paper",
            "Pens",
            "Water-based Markers",
            "White Pens"
        )
    }

    val allMaterials: StateFlow<List<ArtMaterial>> = materialRepository.getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedStockFilter = MutableStateFlow(StockFilter.ALL)
    val selectedStockFilter: StateFlow<StockFilter> = _selectedStockFilter.asStateFlow()

    val availableCategories: StateFlow<List<String>> =
        categoryRepository.getAllCategoryNames()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FIXED_CATEGORIES
            )

    val filteredMaterials: StateFlow<List<ArtMaterial>> = allMaterials
        .combine(_searchQuery) { materials, query ->
            if (query.isBlank()) materials
            else materials.filter { material ->
                material.name.contains(query, ignoreCase = true) ||
                material.description.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedCategory) { searchResults, category ->
            if (category == "All") searchResults
            else searchResults.filter { it.category == category }
        }
        .combine(_selectedStockFilter) { categoryResults, stockFilter ->
            when (stockFilter) {
                StockFilter.ALL      -> categoryResults
                StockFilter.IN_STOCK -> categoryResults.filter { it.quantity > 0 }
                StockFilter.FINISHED -> categoryResults.filter { it.quantity == 0 }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categoryStats: StateFlow<List<CategoryStat>> =
        allMaterials.combine(availableCategories) { materials, categories ->
            if (materials.isEmpty()) {
                emptyList()
            } else {
                val inStockMaterials = materials.filter { it.quantity > 0 }
                val totalUnits = inStockMaterials.sumOf { it.quantity }
                categories
                    .map { category ->
                        val categoryMaterials = inStockMaterials.filter { it.category == category }
                        val units = categoryMaterials.sumOf { it.quantity }
                        val distinctUnits = categoryMaterials.map { it.unit.trim().lowercase() }.toSet()
                        val unitLabel = if (distinctUnits.size == 1) categoryMaterials.first().unit.trim() else "units"
                        val pct = if (totalUnits > 0) (units.toFloat() / totalUnits) * 100f else 0f
                        CategoryStat(category, units, pct, unitLabel)
                    }
                    .filter { it.units > 0 }
                    .sortedByDescending { it.units }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalUnits: StateFlow<Int> = allMaterials
        .map { materials -> materials.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val logEntries: StateFlow<List<UsageLog>> =
        logRepository.getAllLogs()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _backupResult = MutableStateFlow<BackupResult?>(null)
    val backupResult: StateFlow<BackupResult?> = _backupResult.asStateFlow()

    fun clearBackupResult() { _backupResult.value = null }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun selectCategory(category: String) { _selectedCategory.value = category }

    fun selectStockFilter(filter: StockFilter) { _selectedStockFilter.value = filter }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            backupRepository.exportToUri(uri)
                .onSuccess { _backupResult.value = BackupResult.ExportSuccess }
                .onFailure { _backupResult.value = BackupResult.Failure(it.message ?: "Export failed") }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupRepository.importFromUri(uri)
                .onSuccess { summary ->
                    _backupResult.value = BackupResult.ImportSuccess(
                        materialsAdded = summary.materialsImported,
                        logsAdded      = summary.logsImported
                    )
                }
                .onFailure { _backupResult.value = BackupResult.Failure(it.message ?: "Import failed") }
        }
    }

    private val _deletedLogEntry = MutableSharedFlow<UsageLog>(extraBufferCapacity = 1)
    val deletedLogEntry: SharedFlow<UsageLog> = _deletedLogEntry.asSharedFlow()

    fun clearJournal() {
        viewModelScope.launch {
            logRepository.clearAllLogs()
        }
    }

    fun deleteJournalEntry(log: UsageLog) {
        viewModelScope.launch {
            logRepository.deleteLogById(log.id)
            _deletedLogEntry.emit(log)
        }
    }

    fun undoDeleteJournalEntry(log: UsageLog) {
        viewModelScope.launch {
            logRepository.insertLog(log)
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.addCategory(name)
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(name)
            if (_selectedCategory.value == name) {
                _selectedCategory.value = "All"
            }
        }
    }

    fun addMaterial(
        name: String,
        brand: String,
        quantity: Int,
        category: String,
        unit: String = "unit",
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val internalUri = imageUri?.let { uriString ->
                    if (isInternalImagePath(context, uriString)) uriString
                    else copyImageToInternalStorage(context, uriString.toUri()) ?: uriString
                }

                val result = materialRepository.addMaterial(name, brand, quantity, category, unit, internalUri)
                result.onSuccess { material ->
                    writeLog(
                        material     = material,
                        eventType    = EventType.ADDED,
                        delta        = material.quantity,
                        qtyAfter     = material.quantity
                    )
                }
                result.onFailure { _errorMessage.value = it.message ?: "Failed to add material" }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMaterial(materialId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val snapshot = allMaterials.value.firstOrNull { it.id == materialId }

                val result = materialRepository.deleteMaterial(materialId)
                result.onSuccess {
                    if (snapshot != null) {
                        deleteInternalImage(context, snapshot.photoUri)
                        writeLog(
                            material  = snapshot,
                            eventType = EventType.DELETED,
                            delta     = -snapshot.quantity,
                            qtyAfter  = 0
                        )
                    }
                }
                result.onFailure { _errorMessage.value = it.message ?: "Failed to delete material" }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMaterial(material: ArtMaterial) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val old = allMaterials.value.firstOrNull { it.id == material.id }
                val oldQty = old?.quantity ?: material.quantity

                val newPhotoUri: String? = material.photoUri?.let { uriString ->
                    if (isInternalImagePath(context, uriString)) uriString
                    else {
                        val copied = copyImageToInternalStorage(context, uriString.toUri())
                        if (copied != null && old?.photoUri != uriString) {
                            deleteInternalImage(context, old?.photoUri)
                        }
                        copied ?: uriString
                    }
                }
                val updatedMaterial = material.copy(photoUri = newPhotoUri)

                val result = materialRepository.updateMaterial(updatedMaterial)
                result.onSuccess {
                    val delta = updatedMaterial.quantity - oldQty
                    when {
                        delta > 0 -> writeLog(
                            material  = updatedMaterial,
                            eventType = EventType.RESTOCKED,
                            delta     = delta,
                            qtyAfter  = updatedMaterial.quantity
                        )
                        delta < 0 -> writeLog(
                            material  = updatedMaterial,
                            eventType = EventType.USED,
                            delta     = delta,
                            qtyAfter  = updatedMaterial.quantity
                        )
                    }
                }
                result.onFailure { _errorMessage.value = it.message ?: "Failed to update material" }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun writeLog(
        material: ArtMaterial,
        eventType: String,
        delta: Int,
        qtyAfter: Int
    ) {
        logRepository.insertLog(
            UsageLog(
                id            = UUID.randomUUID().toString(),
                materialId    = material.id,
                materialName  = material.name,
                category      = material.category,
                eventType     = eventType,
                quantityDelta = delta,
                quantityAfter = qtyAfter
            )
        )
    }
}
