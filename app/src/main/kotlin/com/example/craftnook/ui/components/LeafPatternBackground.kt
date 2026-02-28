package com.example.craftnook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toBitmap
import com.example.craftnook.R
import kotlin.math.roundToInt

/**
 * Tiled leaf pattern overlay.
 *
 * Draws the ic_leaf vector in a diagonal grid at extremely low opacity (3–5%),
 * alternating rotation between instances to create a scattered botanical feel.
 *
 * @param opacity    Alpha for the entire pattern. Keep between 0.03f–0.05f.
 * @param tileSize   Distance between tile centres (larger = more spaced out).
 * @param leafSize   Rendered size of each leaf in dp.
 */
@Composable
fun LeafPatternBackground(
    modifier: Modifier = Modifier,
    opacity: Float = 0.04f,
    tileSize: Dp = 96.dp,
    leafSize: Dp = 36.dp
) {
    val context = LocalContext.current

    // Rasterise the vector drawable once into a bitmap, tinted brown.
    val leafBitmap = remember(leafSize) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_leaf)!!
        val px = (leafSize.value * context.resources.displayMetrics.density).roundToInt()
            .coerceAtLeast(1)
        drawable.toBitmap(width = px, height = px)
    }
    val imageBitmap = remember(leafBitmap) { leafBitmap.asImageBitmap() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val tilePx   = tileSize.toPx()
        val leafPx   = leafSize.toPx()
        val halfLeaf = leafPx / 2f

        // Rotations applied to alternate tiles for a scattered look
        val rotations = listOf(0f, 45f, -30f, 90f, 20f, -60f)

        val cols = (size.width  / tilePx + 2).toInt()
        val rows = (size.height / tilePx + 2).toInt()

        for (row in -1..rows) {
            for (col in -1..cols) {
                // Offset every other row by half a tile (brick pattern)
                val xOffset = if (row % 2 == 0) 0f else tilePx * 0.5f
                val cx = col * tilePx + xOffset
                val cy = row * tilePx

                val rotation = rotations[(row * cols + col).coerceAtLeast(0) % rotations.size]

                withTransform({
                    translate(cx - halfLeaf, cy - halfLeaf)
                    rotate(rotation, pivot = Offset(halfLeaf, halfLeaf))
                }) {
                    drawImage(
                        image = imageBitmap,
                        colorFilter = ColorFilter.tint(
                            Color(0xFF795548).copy(alpha = opacity)
                        )
                    )
                }
            }
        }
    }
}
