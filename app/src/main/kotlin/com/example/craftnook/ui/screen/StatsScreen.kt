package com.example.craftnook.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.craftnook.ui.theme.BackgroundLight
import com.example.craftnook.ui.theme.CategoryAlcoholMarkersColor
import com.example.craftnook.ui.theme.CategoryA4NotebooksColor
import com.example.craftnook.ui.theme.CategoryA5NotebooksColor
import com.example.craftnook.ui.theme.CategoryBrushesColor
import com.example.craftnook.ui.theme.CategoryColoredPencilsColor
import com.example.craftnook.ui.theme.CategoryCrayonsColor
import com.example.craftnook.ui.theme.CategoryDrawingPencilsColor
import com.example.craftnook.ui.theme.CategoryErasersColor
import com.example.craftnook.ui.theme.CategoryFinelinersColor
import com.example.craftnook.ui.theme.CategoryFountainPenCartridgesColor
import com.example.craftnook.ui.theme.CategoryGlitterPensColor
import com.example.craftnook.ui.theme.CategoryHighlightersColor
import com.example.craftnook.ui.theme.CategoryMechanicalPencilLeadsColor
import com.example.craftnook.ui.theme.CategoryMechanicalPencilsColor
import com.example.craftnook.ui.theme.CategoryMetallicPensColor
import com.example.craftnook.ui.theme.CategoryPaintColor
import com.example.craftnook.ui.theme.CategoryPaperColor
import com.example.craftnook.ui.theme.CategoryPensColor
import com.example.craftnook.ui.theme.CategoryWaterbasedMarkersColor
import com.example.craftnook.ui.theme.CategoryWhitePensColor
import com.example.craftnook.ui.theme.OnBackgroundLight
import com.example.craftnook.ui.theme.PrimaryContainerLight
import com.example.craftnook.ui.theme.PrimaryLight
import com.example.craftnook.ui.viewmodel.CategoryStat
import com.example.craftnook.ui.viewmodel.InventoryViewModel

// ── Colour helpers ──────────────────────────────────────────────────────────

private fun getCategoryBarColor(category: String): Color = when (category) {
    "Paint"                    -> CategoryPaintColor
    "Brushes"                  -> CategoryBrushesColor
    "Paper"                    -> CategoryPaperColor
    "Pens"                     -> CategoryPensColor
    "Alcohol Markers"          -> CategoryAlcoholMarkersColor
    "Water-based Markers"      -> CategoryWaterbasedMarkersColor
    "Colored Pencils"          -> CategoryColoredPencilsColor
    "Drawing Pencils"          -> CategoryDrawingPencilsColor
    "Mechanical Pencil Leads"  -> CategoryMechanicalPencilLeadsColor
    "Mechanical Pencils"       -> CategoryMechanicalPencilsColor
    "White Pens"               -> CategoryWhitePensColor
    "Glitter Pens"             -> CategoryGlitterPensColor
    "Metallic Pens"            -> CategoryMetallicPensColor
    "Crayons"                  -> CategoryCrayonsColor
    "Highlighters"             -> CategoryHighlightersColor
    "A4 Notebooks"             -> CategoryA4NotebooksColor
    "A5 Notebooks"             -> CategoryA5NotebooksColor
    "Fountain Pen Cartridges"  -> CategoryFountainPenCartridgesColor
    "Fineliners"               -> CategoryFinelinersColor
    "Erasers"                  -> CategoryErasersColor
    else                       -> PrimaryLight
}

// ── Darken helper (for bar border accent) ──────────────────────────────────

private fun Color.darken(factor: Float = 0.25f): Color {
    val argb = this.toArgb()
    val r = ((argb shr 16 and 0xFF) * (1f - factor)).toInt().coerceIn(0, 255)
    val g = ((argb shr 8  and 0xFF) * (1f - factor)).toInt().coerceIn(0, 255)
    val b = ((argb        and 0xFF) * (1f - factor)).toInt().coerceIn(0, 255)
    return Color(r, g, b)
}

// ── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: InventoryViewModel) {
    val categoryStats by viewModel.categoryStats.collectAsState()
    val allMaterials  by viewModel.allMaterials.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stats",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor     = PrimaryContainerLight,
                    titleContentColor  = OnBackgroundLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (categoryStats.isEmpty()) {
                EmptyStatsState()
            } else {
                // Summary cards row
                SummaryRow(
                    totalItems      = allMaterials.size,
                    categoriesUsed  = categoryStats.size,
                    topCategory     = categoryStats.firstOrNull()?.category ?: ""
                )

                // Bar chart card
                BarChartCard(stats = categoryStats)

                // Legend / breakdown list
                LegendCard(stats = categoryStats)
            }
        }
    }
}

// ── Summary row ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryRow(
    totalItems: Int,
    categoriesUsed: Int,
    topCategory: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label  = "Total items",
            value  = totalItems.toString(),
            color  = PrimaryLight
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label  = "Categories",
            value  = categoriesUsed.toString(),
            color  = CategoryColoredPencilsColor.darken(0.08f)
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label  = "Top category",
            value  = topCategory,
            color  = CategoryAlcoholMarkersColor.darken(0.08f),
            smallText = true
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    smallText: Boolean = false
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = if (smallText)
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                else
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color      = OnBackgroundLight,
                textAlign  = TextAlign.Center,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnBackgroundLight.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Bar chart card ───────────────────────────────────────────────────────────

@Composable
private fun BarChartCard(stats: List<CategoryStat>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)) {
            Text(
                text  = "Items per category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnBackgroundLight
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Number of distinct material entries in each category",
                style = MaterialTheme.typography.bodySmall,
                color = OnBackgroundLight.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(20.dp))

            // Horizontally scrollable chart so all bars fit nicely
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                BarChart(stats = stats)
            }
        }
    }
}

// Bar width + spacing constants
private val BAR_WIDTH   = 46.dp
private val BAR_SPACING = 12.dp
private val CHART_HEIGHT = 220.dp
private val LABEL_HEIGHT = 56.dp   // reserved below bars for rotated labels

@Composable
private fun BarChart(stats: List<CategoryStat>) {
    val maxCount = stats.maxOf { it.count }.coerceAtLeast(1)

    // One Animatable per bar so they animate in sequence
    val animatables = remember(stats) { stats.map { Animatable(0f) } }
    LaunchedEffect(stats) {
        animatables.forEachIndexed { index, anim ->
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis    = index * 60
                )
            )
        }
    }

    val totalWidth = (BAR_WIDTH + BAR_SPACING) * stats.size + BAR_SPACING

    Row(
        modifier            = Modifier.width(totalWidth),
        horizontalArrangement = Arrangement.spacedBy(BAR_SPACING),
        verticalAlignment   = Alignment.Bottom
    ) {
        stats.forEachIndexed { index, stat ->
            val animProgress = animatables[index].value
            val barColor     = getCategoryBarColor(stat.category)

            Column(
                modifier            = Modifier.width(BAR_WIDTH),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Count label above bar
                Text(
                    text  = stat.count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnBackgroundLight
                )
                Spacer(Modifier.height(4.dp))

                // Animated bar
                val barHeightFraction = (stat.count.toFloat() / maxCount) * animProgress
                Box(
                    modifier = Modifier
                        .width(BAR_WIDTH)
                        .height(CHART_HEIGHT * barHeightFraction.coerceAtLeast(0.04f))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(barColor)
                )

                Spacer(Modifier.height(6.dp))

                // Category label — truncated to 2 lines
                Text(
                    text      = stat.category,
                    style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color     = OnBackgroundLight.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    maxLines  = 2,
                    overflow  = TextOverflow.Ellipsis,
                    modifier  = Modifier
                        .width(BAR_WIDTH)
                        .height(LABEL_HEIGHT)
                )
            }
        }
    }
}

// ── Legend / breakdown list ─────────────────────────────────────────────────

@Composable
private fun LegendCard(stats: List<CategoryStat>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text  = "Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnBackgroundLight
            )
            Spacer(Modifier.height(12.dp))

            stats.forEach { stat ->
                LegendRow(stat = stat)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun LegendRow(stat: CategoryStat) {
    val barColor = getCategoryBarColor(stat.category)

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colour dot
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(barColor)
        )
        Spacer(Modifier.width(10.dp))

        // Category name
        Text(
            text     = stat.category,
            style    = MaterialTheme.typography.bodyMedium,
            color    = OnBackgroundLight,
            modifier = Modifier.weight(1f)
        )

        // Progress bar + count
        Box(
            modifier = Modifier
                .weight(1.5f)
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(barColor.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(stat.percentage / 100f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(barColor)
            )
        }
        Spacer(Modifier.width(8.dp))

        Text(
            text  = "${stat.count} (${stat.percentage.toInt()}%)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = OnBackgroundLight,
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyStatsState() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector       = Icons.Filled.BarChart,
            contentDescription = null,
            modifier          = Modifier.size(72.dp),
            tint              = PrimaryLight.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "No data yet",
            style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color     = OnBackgroundLight
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Add some materials to your inventory\nand your stats will appear here.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = OnBackgroundLight.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}
