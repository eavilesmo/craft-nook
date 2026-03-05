package com.example.craftnook.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.vector.ImageVector

internal fun getCategoryIcon(category: String): ImageVector = when (category) {
    "Paint"                    -> Icons.Filled.Palette
    "Brushes"                  -> Icons.Filled.Brush
    "Paper"                    -> Icons.Filled.Note
    "Pens"                     -> Icons.Filled.Edit
    "Alcohol Markers"          -> Icons.Filled.Edit
    "Water-based Markers"      -> Icons.Filled.Edit
    "Colored Pencils"          -> Icons.Filled.Edit
    "Drawing Pencils"          -> Icons.Filled.Edit
    "Mechanical Pencil Leads"  -> Icons.Filled.Edit
    "Mechanical Pencils"       -> Icons.Filled.Edit
    "White Pens"               -> Icons.Filled.Edit
    "Glitter Pens"             -> Icons.Filled.Edit
    "Metallic Pens"            -> Icons.Filled.Edit
    "Crayons"                  -> Icons.Filled.Edit
    "Highlighters"             -> Icons.Filled.Highlight
    "A4 Notebooks"             -> Icons.Filled.Note
    "A5 Notebooks"             -> Icons.Filled.Note
    "Fountain Pen Cartridges"  -> Icons.Filled.Edit
    "Fineliners"               -> Icons.Filled.Edit
    "Erasers"                  -> Icons.Filled.Edit
    else                       -> Icons.Filled.Palette
}
