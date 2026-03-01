package com.example.craftnook.ui.viewmodel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ── Supporting data classes ──────────────────────────────────────────────────

/** Per-category statistics used by the Stats screen. */
data class CategoryStat(
    val category: String,
    val units: Int,
    val percentage: Float
)

/**
 * Holds context for a pending quantity-reduction edit so the UI can ask
 * "Did you use this material, or are you correcting a mistake?" before
 * committing the log entry.
 */
data class PendingQuantityConfirmation(
    val material: ArtMaterial,
    val oldQuantity: Int,
    val newQuantity: Int
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
            materials.filter { material ->
                material.name.contains(query, ignoreCase = true) ||
                material.description.contains(query, ignoreCase = true)
            }
        }
        .combine(_selectedCategory) { searchResults, category ->
            if (category == "All") searchResults
            else searchResults.filter { it.category == category }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categoryStats: StateFlow<List<CategoryStat>> =
        allMaterials.combine(availableCategories) { materials, categories ->
            val totalUnits = materials.sumOf { it.quantity }
            if (totalUnits == 0) {
                emptyList()
            } else {
                categories
                    .map { category ->
                        val units = materials
                            .filter { it.category == category }
                            .sumOf { it.quantity }
                        CategoryStat(category, units, (units.toFloat() / totalUnits) * 100f)
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

    /**
     * Set when a quantity-reducing edit is saved. The UI observes this to
     * show the "Did you use this / Just correcting?" dialog.
     * Cleared by [confirmQuantityChange].
     */
    private val _pendingQuantityConfirmation = MutableStateFlow<PendingQuantityConfirmation?>(null)
    val pendingQuantityConfirmation: StateFlow<PendingQuantityConfirmation?> =
        _pendingQuantityConfirmation.asStateFlow()

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
     */
    fun addMaterial(
        name: String,
        brand: String,
        quantity: Int,
        category: String,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = materialRepository.addMaterial(name, brand, quantity, category, imageUri)
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
     * Delete a material and write a DELETED log entry capturing the final quantity.
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
     * - If quantity increased → write RESTOCKED automatically.
     * - If quantity decreased → surface [PendingQuantityConfirmation] so the UI
     *   can ask the user whether this was real usage or a correction.
     * - If quantity unchanged → no log entry.
     * - Other field changes (name, brand, photo…) also produce no log entry.
     */
    fun updateMaterial(material: ArtMaterial) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val oldQty = allMaterials.value
                    .firstOrNull { it.id == material.id }
                    ?.quantity ?: material.quantity

                val result = materialRepository.updateMaterial(material)
                result.onSuccess {
                    val delta = material.quantity - oldQty
                    when {
                        delta > 0 -> writeLog(
                            material  = material,
                            eventType = EventType.RESTOCKED,
                            delta     = delta,
                            qtyAfter  = material.quantity
                        )
                        delta < 0 -> {
                            // Ask user: used or correction?
                            _pendingQuantityConfirmation.value = PendingQuantityConfirmation(
                                material    = material,
                                oldQuantity = oldQty,
                                newQuantity = material.quantity
                            )
                        }
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

    /**
     * Called by the UI dialog after the user answers "Did you use this?"
     *
     * @param wasUsed true → USED event; false → RESTOCKED (correction) event.
     */
    fun confirmQuantityChange(wasUsed: Boolean) {
        val pending = _pendingQuantityConfirmation.value ?: return
        _pendingQuantityConfirmation.value = null
        viewModelScope.launch {
            val delta = pending.newQuantity - pending.oldQuantity  // negative
            writeLog(
                material  = pending.material,
                eventType = if (wasUsed) EventType.USED else EventType.RESTOCKED,
                delta     = delta,
                qtyAfter  = pending.newQuantity
            )
        }
    }

    /** Dismiss the confirmation dialog without writing any log entry. */
    fun dismissQuantityConfirmation() {
        _pendingQuantityConfirmation.value = null
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
