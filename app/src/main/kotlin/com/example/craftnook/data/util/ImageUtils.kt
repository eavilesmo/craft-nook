package com.example.craftnook.data.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Copies the image at [sourceUri] into the app's private files directory
 * (`filesDir/images/<uuid>.jpg`) and returns the absolute path of the copy,
 * or null if the copy failed.
 *
 * Storing the file privately means:
 *  - It persists independently of the device gallery.
 *  - No READ_EXTERNAL_STORAGE permission is required.
 *  - The file is included in Android Auto-Backup by default.
 */
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

/**
 * Returns true if [path] already points to a file inside the app's private
 * images directory — i.e. it was already copied and does not need to be
 * copied again.
 */
fun isInternalImagePath(context: Context, path: String): Boolean {
    val imagesDir = File(context.filesDir, "images")
    return path.startsWith(imagesDir.absolutePath)
}

/**
 * Deletes the internal image file at [path] if it lives inside the app's
 * private images directory. Safe to call with null or external URIs.
 */
fun deleteInternalImage(context: Context, path: String?) {
    if (path == null) return
    if (isInternalImagePath(context, path)) {
        File(path).delete()
    }
}
