package com.example.craftnook.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

private fun Color.darken(factor: Float = 0.25f): Color {
    val argb = this.toArgb()
    val r = ((argb shr 16 and 0xFF) * (1f - factor)).toInt().coerceIn(0, 255)
    val g = ((argb shr 8  and 0xFF) * (1f - factor)).toInt().coerceIn(0, 255)
    val b = ((argb        and 0xFF) * (1f - factor)).toInt().coerceIn(0, 255)
    return Color(r, g, b)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: InventoryViewModel) {
    val categoryStats by viewModel.categoryStats.collectAsState()
    val allMaterials  by viewModel.allMaterials.collectAsState()
    val totalUnits    by viewModel.totalUnits.collectAsState()

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
                    totalUnits      = totalUnits,
                    categoriesUsed  = categoryStats.size,
                    topCategory     = categoryStats.firstOrNull()?.category ?: ""
                )

                // Breakdown list — now the main focus
                BreakdownCard(stats = categoryStats)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    totalItems: Int,
    totalUnits: Int,
    categoriesUsed: Int,
    topCategory: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label    = "Total entries",
            value    = totalItems.toString(),
            color    = PrimaryLight
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label    = "Total units",
            value    = totalUnits.toString(),
            color    = CategoryFinelinersColor.darken(0.05f)
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label    = "Categories",
            value    = categoriesUsed.toString(),
            color    = CategoryColoredPencilsColor.darken(0.08f)
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label    = "Top category",
            value    = topCategory,
            color    = CategoryAlcoholMarkersColor.darken(0.08f)
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text      = value,
                style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color     = OnBackgroundLight,
                textAlign = TextAlign.Center,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = OnBackgroundLight.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BreakdownCard(stats: List<CategoryStat>) {
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
            Spacer(Modifier.height(16.dp))

            stats.forEachIndexed { index, stat ->
                BreakdownRow(stat = stat)
                if (index < stats.lastIndex) {
                    Spacer(Modifier.height(18.dp))
                }
            }
        }
    }
}

private val UNIT_COUNT_WIDTH = 96.dp
private val PERCENTAGE_WIDTH = 40.dp

@Composable
private fun BreakdownRow(stat: CategoryStat) {
    val barColor  = getCategoryBarColor(stat.category)
    val icon      = getCategoryIcon(stat.category)

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(barColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = barColor,
                    modifier           = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text     = stat.category,
                style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color    = OnBackgroundLight,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text      = "${stat.units} ${stat.unitLabel}",
                style     = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color     = OnBackgroundLight,
                textAlign = TextAlign.End,
                maxLines  = 1,
                modifier  = Modifier.width(UNIT_COUNT_WIDTH)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text      = "${stat.percentage.toInt()}%",
                style     = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color     = barColor,
                textAlign = TextAlign.End,
                modifier  = Modifier.width(PERCENTAGE_WIDTH)
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CircleShape)
                .background(barColor.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stat.percentage / 100f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

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
            imageVector        = Icons.Filled.BarChart,
            contentDescription = null,
            modifier           = Modifier.size(72.dp),
            tint               = PrimaryLight.copy(alpha = 0.5f)
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
