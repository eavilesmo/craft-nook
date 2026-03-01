package com.example.craftnook.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.craftnook.data.database.ArtMaterialDao
import com.example.craftnook.data.database.UsageLog
import com.example.craftnook.data.database.UsageLogDao
import com.example.craftnook.data.util.isInternalImagePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

// ── Backup envelope ──────────────────────────────────────────────────────────

/**
 * Per-material image blob included in the backup.
 * [materialId] links back to [ArtMaterial.id].
 * [base64Data] is the raw image bytes encoded as Base64 (no line breaks).
 */
@Serializable
data class BackupImage(
    val materialId: String,
    val base64Data: String
)

/** Top-level JSON structure written to / read from the backup file. */
@Serializable
data class BackupData(
    val schemaVersion: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val materials: List<ArtMaterial> = emptyList(),
    val usageLogs: List<UsageLog> = emptyList(),
    /** Image blobs, keyed by material ID. Present only when schemaVersion >= 2. */
    val images: List<BackupImage> = emptyList()
)

/** Summary returned to the ViewModel after a successful import. */
data class ImportSummary(
    val materialsImported: Int,
    val logsImported: Int
)

// ── Repository interface ─────────────────────────────────────────────────────

interface IBackupRepository {
    /**
     * Serialize all materials + journal entries (+ embedded images) and write
     * JSON to [uri].  The caller must have already obtained a writable SAF URI
     * via [ACTION_CREATE_DOCUMENT].
     */
    suspend fun exportToUri(uri: Uri): Result<Unit>

    /**
     * Read a backup JSON file from [uri] and insert all records into Room,
     * skipping any whose ID already exists (IGNORE conflict strategy).
     * Images embedded in the backup are written back into private storage.
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

            // Embed image bytes for every material that has an internal image file
            val images = materials.mapNotNull { material ->
                val path = material.photoUri ?: return@mapNotNull null
                if (!isInternalImagePath(context, path)) return@mapNotNull null
                val file = File(path)
                if (!file.exists()) return@mapNotNull null
                val bytes = file.readBytes()
                BackupImage(
                    materialId = material.id,
                    base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
                )
            }

            val backup = BackupData(
                materials = materials,
                usageLogs = logs,
                images    = images
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

            // Restore image files first so photoUri paths are valid when inserted
            val restoredPaths = mutableMapOf<String, String>() // materialId → absolutePath
            val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
            backup.images.forEach { backupImage ->
                val destFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
                val bytes = Base64.decode(backupImage.base64Data, Base64.NO_WRAP)
                destFile.writeBytes(bytes)
                restoredPaths[backupImage.materialId] = destFile.absolutePath
            }

            var materialsInserted = 0
            backup.materials.forEach { material ->
                // Remap photoUri to the newly restored file path (if image was in backup)
                val remappedMaterial = restoredPaths[material.id]?.let { newPath ->
                    material.copy(photoUri = newPath)
                } ?: material

                val rowId = materialDao.insertMaterialIfNotExists(remappedMaterial)
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

