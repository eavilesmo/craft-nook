package com.example.craftnook.data.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
        val destFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        destFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

fun isInternalImagePath(context: Context, path: String): Boolean {
    val imagesDir = File(context.filesDir, "images")
    return path.startsWith(imagesDir.absolutePath)
}

fun deleteInternalImage(context: Context, path: String?) {
    if (path == null) return
    if (isInternalImagePath(context, path)) {
        File(path).delete()
    }
}
