package com.example.craftnook.data.repository

import android.content.Context
import android.net.Uri
import com.example.craftnook.data.database.ArtMaterialDao
import com.example.craftnook.data.database.UsageLog
import com.example.craftnook.data.database.UsageLogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ── Backup envelope ──────────────────────────────────────────────────────────

/** Top-level JSON structure written to / read from the backup file. */
@Serializable
data class BackupData(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val materials: List<ArtMaterial> = emptyList(),
    val usageLogs: List<UsageLog> = emptyList()
)

/** Summary returned to the ViewModel after a successful import. */
data class ImportSummary(
    val materialsImported: Int,
    val logsImported: Int
)

// ── Repository interface ─────────────────────────────────────────────────────

interface IBackupRepository {
    /**
     * Serialize all materials + journal entries and write JSON to [uri].
     * The caller must have already obtained a writable SAF URI via
     * [ACTION_CREATE_DOCUMENT].
     */
    suspend fun exportToUri(uri: Uri): Result<Unit>

    /**
     * Read a backup JSON file from [uri] and insert all records into Room,
     * skipping any whose ID already exists (IGNORE conflict strategy).
     *
     * @return [ImportSummary] containing counts of newly inserted rows.
     */
    suspend fun importFromUri(uri: Uri): Result<ImportSummary>
}

// ── Room implementation ──────────────────────────────────────────────────────

class RoomBackupRepository(
    private val context: Context,
    private val materialDao: ArtMaterialDao,
    private val usageLogDao: UsageLogDao
) : IBackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true   // forward-compatible: future fields won't break old backups
        encodeDefaults = true
    }

    override suspend fun exportToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val materials = materialDao.getAllMaterials().first()
            val logs      = usageLogDao.getAllLogs().first()

            val backup = BackupData(
                materials = materials,
                usageLogs = logs
            )
            val jsonString = json.encodeToString(backup)

            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(jsonString)
            } ?: error("Could not open output stream for URI: $uri")
        }
    }

    override suspend fun importFromUri(uri: Uri): Result<ImportSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: error("Could not open input stream for URI: $uri")

            val backup = json.decodeFromString<BackupData>(jsonString)

            var materialsInserted = 0
            backup.materials.forEach { material ->
                val rowId = materialDao.insertMaterialIfNotExists(material)
                if (rowId != -1L) materialsInserted++
            }

            var logsInserted = 0
            backup.usageLogs.forEach { log ->
                val rowId = usageLogDao.insertIfNotExists(log)
                if (rowId != -1L) logsInserted++
            }

            ImportSummary(materialsInserted, logsInserted)
        }
    }
}
