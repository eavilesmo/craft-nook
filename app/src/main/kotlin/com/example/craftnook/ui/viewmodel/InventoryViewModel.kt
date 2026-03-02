package com.example.craftnook.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ── Supporting data classes ──────────────────────────────────────────────────

/** Controls the stock-status filter on the Inventory screen. */
enum class StockFilter { ALL, IN_STOCK, FINISHED }

/** Per-category statistics used by the Stats screen. */
data class CategoryStat(
    val category: String,
    val units: Int,
    val percentage: Float,
    /** Display label for the unit. When all materials in this category share the
     *  same unit string that unit is used (e.g. "markers", "tubes"); otherwise
     *  falls back to the generic "units". */
    val unitLabel: String = "units"
)

/** Outcome of a backup export or import operation, shown to the user as a snackbar. */
sealed class BackupResult {
    object ExportSuccess : BackupResult()
    data class ImportSuccess(val materialsAdded: Int, val logsAdded: Int) : BackupResult()
    data class Failure(val message: String) : BackupResult()
}

// ── ViewModel ────────────────────────────────────────────────────────────────

/**
 * ViewModel for inventory management and usage journal.
 *
 * All mutating actions (add / update / delete) automatically write a
 * [UsageLog] entry. When an edit reduces quantity the ViewModel surfaces a
 * [PendingQuantityConfirmation] event so the UI can ask the user whether
 * the reduction represents real usage or just a correction.
 *
 * Categories are now backed by a Room table ([ICategoryRepository]) instead
 * of a hardcoded list. The [availableCategories] flow always emits them
 * sorted A→Z (the DAO handles ordering).
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val materialRepository: IArtMaterialRepository,
    private val logRepository: IUsageLogRepository,
    private val categoryRepository: ICategoryRepository,
    private val backupRepository: IBackupRepository
) : ViewModel() {

    companion object {
        /**
         * Kept for reference / backward-compat (e.g. migration seed).
         * The live source of truth is now the Room [categories] table.
         */
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

    // ── Inventory state ──────────────────────────────────────────────────────

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

    /**
     * Live, alphabetically sorted list of categories from Room.
     * The DAO orders the query by name ASC, so this is always A→Z.
     */
    val availableCategories: StateFlow<List<String>> =
        categoryRepository.getAllCategoryNames()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FIXED_CATEGORIES   // shown immediately before DB loads
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
                // Only count in-stock units for the breakdown — finished items are excluded.
                val inStockMaterials = materials.filter { it.quantity > 0 }
                val totalUnits = inStockMaterials.sumOf { it.quantity }
                categories
                    .map { category ->
                        val categoryMaterials = inStockMaterials.filter { it.category == category }
                        val units = categoryMaterials.sumOf { it.quantity }
                        // Use the shared unit name if every item in this category has the
                        // same unit string; otherwise fall back to the generic "units".
                        val distinctUnits = categoryMaterials.map { it.unit.trim().lowercase() }.toSet()
                        val unitLabel = if (distinctUnits.size == 1) categoryMaterials.first().unit.trim() else "units"
                        val pct = if (totalUnits > 0) (units.toFloat() / totalUnits) * 100f else 0f
                        CategoryStat(category, units, pct, unitLabel)
                    }
                    // Only show categories that have at least one in-stock item.
                    .filter { it.units > 0 }
                    .sortedByDescending { it.units }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Sum of all material quantities across the entire inventory. */
    val totalUnits: StateFlow<Int> = allMaterials
        .map { materials -> materials.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ── Journal / log state ──────────────────────────────────────────────────

    /** All journal entries, newest first. */
    val logEntries: StateFlow<List<UsageLog>> =
        logRepository.getAllLogs()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ── Loading / error state ────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Backup state ─────────────────────────────────────────────────────────

    /**
     * One-shot result of the latest export or import operation.
     * The UI consumes this (calls [clearBackupResult]) after showing the snackbar.
     */
    private val _backupResult = MutableStateFlow<BackupResult?>(null)
    val backupResult: StateFlow<BackupResult?> = _backupResult.asStateFlow()

    fun clearBackupResult() { _backupResult.value = null }

    // ── Public actions ───────────────────────────────────────────────────────

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun selectCategory(category: String) { _selectedCategory.value = category }

    fun selectStockFilter(filter: StockFilter) { _selectedStockFilter.value = filter }

    fun clearError() { _errorMessage.value = null }

    // ── Backup actions ───────────────────────────────────────────────────────

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

    // ── Journal actions ──────────────────────────────────────────────────────

    /**
     * Emits the most recently deleted [UsageLog] so the UI can offer an Undo
     * snackbar. Collecting the emission and calling [undoDeleteJournalEntry]
     * re-inserts the row without touching inventory quantities.
     */
    private val _deletedLogEntry = MutableSharedFlow<UsageLog>(extraBufferCapacity = 1)
    val deletedLogEntry: SharedFlow<UsageLog> = _deletedLogEntry.asSharedFlow()

    /** Delete every usage log entry. The [logEntries] Flow updates instantly. */
    fun clearJournal() {
        viewModelScope.launch {
            logRepository.clearAllLogs()
        }
    }

    /**
     * Delete a single journal entry by ID. Does NOT change any inventory
     * quantity — it is purely a history record deletion.
     * Emits the deleted entry via [deletedLogEntry] so the UI can offer Undo.
     */
    fun deleteJournalEntry(log: UsageLog) {
        viewModelScope.launch {
            logRepository.deleteLogById(log.id)
            _deletedLogEntry.emit(log)
        }
    }

    /**
     * Re-insert a previously deleted log entry (Undo action).
     * Uses IGNORE conflict strategy so re-inserting an already-present row
     * is a no-op (safe to call twice).
     */
    fun undoDeleteJournalEntry(log: UsageLog) {
        viewModelScope.launch {
            logRepository.insertLog(log)
        }
    }

    // ── Category management ──────────────────────────────────────────────────

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.addCategory(name)
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(name)
            // If the deleted category is currently selected, reset to "All"
            if (_selectedCategory.value == name) {
                _selectedCategory.value = "All"
            }
        }
    }

    // ── Inventory CRUD ───────────────────────────────────────────────────────

    /**
     * Add a new material and write an ADDED log entry.
     *
     * If [imageUri] is a `content://` URI (from the photo picker) it is first
     * copied into the app's private files directory so the image persists even
     * if the user later deletes it from the gallery.  If it already points to
     * an internal path it is used as-is.
     */
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
                // Copy external content:// URI into private storage once
                val internalUri = imageUri?.let { uriString ->
                    if (isInternalImagePath(context, uriString)) uriString
                    else copyImageToInternalStorage(context, Uri.parse(uriString)) ?: uriString
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

    /**
     * Delete a material, write a DELETED log entry, and remove the internal
     * image file (if any) to avoid orphaned files accumulating.
     */
    fun deleteMaterial(materialId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Snapshot material before deletion so we can log name/qty
                val snapshot = allMaterials.value.firstOrNull { it.id == materialId }

                val result = materialRepository.deleteMaterial(materialId)
                result.onSuccess {
                    if (snapshot != null) {
                        // Clean up private image file
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

    /**
     * Update a material.
     *
     * - If [material.photoUri] is a new `content://` URI it is copied to
     *   private storage and the old internal file (if any) is deleted.
     * - If quantity increased → write a RESTOCKED log entry automatically.
     * - If quantity decreased → write a USED log entry automatically.
     * - If quantity unchanged → no log entry.
     * No dialog is shown; the journal records the change silently.
     */
    fun updateMaterial(material: ArtMaterial) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val old = allMaterials.value.firstOrNull { it.id == material.id }
                val oldQty = old?.quantity ?: material.quantity

                // Copy new image if the URI changed and is not already internal
                val newPhotoUri: String? = material.photoUri?.let { uriString ->
                    if (isInternalImagePath(context, uriString)) uriString
                    else {
                        val copied = copyImageToInternalStorage(context, Uri.parse(uriString))
                        // Delete the old internal file only after the copy succeeds
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
                        // delta == 0 → no log entry needed
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

    fun updateMaterialQuantity(materialId: String, newQuantity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = materialRepository.updateQuantity(materialId, newQuantity)
                result.onFailure { _errorMessage.value = it.message ?: "Failed to update quantity" }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Stub — no usage log table required for this flow. */
    fun getUsageLogs(materialId: String): Flow<List<Nothing>> = emptyFlow()

    // ── Private helpers ──────────────────────────────────────────────────────

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
