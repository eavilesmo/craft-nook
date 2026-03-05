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


@Serializable
data class BackupImage(
    val materialId: String,
    val base64Data: String
)

@Serializable
data class BackupData(
    val schemaVersion: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val materials: List<ArtMaterial> = emptyList(),
    val usageLogs: List<UsageLog> = emptyList(),
    val images: List<BackupImage> = emptyList()
)

data class ImportSummary(
    val materialsImported: Int,
    val logsImported: Int
)


interface IBackupRepository {
    suspend fun exportToUri(uri: Uri): Result<Unit>

    suspend fun importFromUri(uri: Uri): Result<ImportSummary>
}


class RoomBackupRepository(
    private val context: Context,
    private val materialDao: ArtMaterialDao,
    private val usageLogDao: UsageLogDao
) : IBackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun exportToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val materials = materialDao.getAllMaterials().first()
            val logs      = usageLogDao.getAllLogs().first()

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

            val restoredPaths = mutableMapOf<String, String>()
            val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
            backup.images.forEach { backupImage ->
                val destFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
                val bytes = Base64.decode(backupImage.base64Data, Base64.NO_WRAP)
                destFile.writeBytes(bytes)
                restoredPaths[backupImage.materialId] = destFile.absolutePath
            }

            var materialsInserted = 0
            backup.materials.forEach { material ->
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

