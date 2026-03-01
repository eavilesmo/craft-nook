package com.example.craftnook.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.craftnook.ui.theme.OnBackgroundLight
import com.example.craftnook.ui.theme.PrimaryLight
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog shown when the user taps the Settings (gear) icon on the Inventory screen.
 * Provides two sections:
 *  - Export: save a full JSON backup to a file chosen by the user via SAF.
 *  - Import: pick a JSON backup file to restore materials and journal entries.
 *
 * No runtime permissions are needed on API 29+ because the Storage Access
 * Framework grants per-URI access automatically. On API 26–28 the
 * WRITE_EXTERNAL_STORAGE permission declared in the manifest is sufficient.
 */
@Composable
fun BackupRestoreDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    // ── File name: craft_nook_backup_YYYYMMDD_HHmmss.json ──────────────────
    val timestamp  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val exportName = "craft_nook_backup_$timestamp.json"

    // ── Export launcher (ACTION_CREATE_DOCUMENT) ────────────────────────────
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri)
            onDismiss()
        }
    }

    // ── Import launcher (ACTION_OPEN_DOCUMENT) ──────────────────────────────
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text  = "Data Backup",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OnBackgroundLight
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // ── Export section ──────────────────────────────────────────
                BackupSection(
                    icon        = Icons.Filled.Upload,
                    title       = "Export",
                    description = "Save all your materials and journal entries to a JSON file. " +
                                  "You can store it anywhere — cloud storage, email, or local.",
                    buttonLabel = "Export to JSON",
                    isPrimary   = true,
                    onClick     = { exportLauncher.launch(exportName) }
                )

                Spacer(Modifier.height(16.dp))
                Divider(color = OnBackgroundLight.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                // ── Import section ──────────────────────────────────────────
                BackupSection(
                    icon        = Icons.Filled.Download,
                    title       = "Import",
                    description = "Restore from a previously exported JSON file. " +
                                  "Existing entries won't be overwritten — only new items are added.",
                    buttonLabel = "Import from JSON",
                    isPrimary   = false,
                    onClick     = { importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*")) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = OnBackgroundLight.copy(alpha = 0.6f))
            }
        }
    )
}

// ── Private helper composable ────────────────────────────────────────────────

@Composable
private fun BackupSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonLabel: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryLight.copy(alpha = if (isPrimary) 0.18f else 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp),
                    tint               = PrimaryLight
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text  = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = OnBackgroundLight
            )
        }

        Text(
            text  = description,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackgroundLight.copy(alpha = 0.65f)
        )

        if (isPrimary) {
            Button(
                onClick  = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        } else {
            OutlinedButton(
                onClick  = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryLight)
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
