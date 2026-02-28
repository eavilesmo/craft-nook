package com.example.craftnook.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.craftnook.data.database.EventType
import com.example.craftnook.data.database.UsageLog
import com.example.craftnook.ui.theme.BackgroundLight
import com.example.craftnook.ui.theme.ErrorLight
import com.example.craftnook.ui.theme.OnBackgroundLight
import com.example.craftnook.ui.theme.PrimaryContainerLight
import com.example.craftnook.ui.theme.PrimaryLight
import com.example.craftnook.ui.theme.CategoryWaterbasedMarkersColor
import com.example.craftnook.ui.theme.CategoryColoredPencilsColor
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Colour + icon helpers ────────────────────────────────────────────────────

private data class EventStyle(
    val color: Color,
    val icon: ImageVector,
    val label: String
)

private fun eventStyle(eventType: String): EventStyle = when (eventType) {
    EventType.ADDED     -> EventStyle(PrimaryLight,                          Icons.Filled.Add,           "Added")
    EventType.RESTOCKED -> EventStyle(CategoryWaterbasedMarkersColor,         Icons.Filled.Refresh,       "Restocked")
    EventType.USED      -> EventStyle(CategoryColoredPencilsColor,            Icons.Filled.Remove,        "Used")
    EventType.DELETED   -> EventStyle(ErrorLight,                             Icons.Filled.Delete,        "Deleted")
    else                -> EventStyle(Color.Gray,                             Icons.Filled.BookmarkAdded, eventType)
}

// ── Filter chips definition ──────────────────────────────────────────────────

private val ALL_FILTERS = listOf("All", EventType.ADDED, EventType.RESTOCKED, EventType.USED, EventType.DELETED)

private fun filterLabel(filter: String) = when (filter) {
    "All"              -> "All"
    EventType.ADDED    -> "Added"
    EventType.RESTOCKED -> "Restocked"
    EventType.USED     -> "Used"
    EventType.DELETED  -> "Deleted"
    else               -> filter
}

// ── Date formatters ──────────────────────────────────────────────────────────

private val monthFormatter  = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
private val entryFormatter  = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

private fun Long.toMonthKey(): String   = monthFormatter.format(Date(this))
private fun Long.toEntryDate(): String  = entryFormatter.format(Date(this))

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: InventoryViewModel) {
    val allLogs    by viewModel.logEntries.collectAsState()
    var activeFilter by remember { mutableStateOf("All") }

    val filteredLogs = if (activeFilter == "All") allLogs
                       else allLogs.filter { it.eventType == activeFilter }

    // Group by month, preserving newest-first order
    val byMonth: Map<String, List<UsageLog>> = filteredLogs
        .groupBy { it.timestamp.toMonthKey() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Usage Journal",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryContainerLight,
                    titleContentColor = OnBackgroundLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_FILTERS.forEach { filter ->
                    val selected = filter == activeFilter
                    val style    = if (filter == "All") null else eventStyle(filter)
                    FilterChip(
                        selected = selected,
                        onClick  = { activeFilter = filter },
                        label    = { Text(filterLabel(filter), fontSize = 13.sp) },
                        leadingIcon = if (style != null) ({
                            Icon(
                                imageVector        = style.icon,
                                contentDescription = null,
                                modifier           = Modifier.size(16.dp),
                                tint               = if (selected) Color.White else style.color
                            )
                        }) else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor     = style?.color ?: PrimaryLight,
                            selectedLabelColor         = Color.White,
                            selectedLeadingIconColor   = Color.White
                        )
                    )
                }
            }

            if (filteredLogs.isEmpty()) {
                EmptyJournalState(filtered = activeFilter != "All")
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 4.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    byMonth.forEach { (month, entries) ->
                        item(key = month) {
                            MonthSection(month = month, entries = entries)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Month section (collapsible) ──────────────────────────────────────────────

@Composable
private fun MonthSection(month: String, entries: List<UsageLog>) {
    var expanded by remember(month) { mutableStateOf(true) }

    // Build summary line
    val added    = entries.count { it.eventType == EventType.ADDED }
    val used     = entries.count { it.eventType == EventType.USED }
    val restocked = entries.count { it.eventType == EventType.RESTOCKED }
    val deleted  = entries.count { it.eventType == EventType.DELETED }
    val summaryParts = buildList {
        if (added     > 0) add("+$added added")
        if (restocked > 0) add("+$restocked restocked")
        if (used      > 0) add("−$used used")
        if (deleted   > 0) add("$deleted deleted")
    }
    val summary = summaryParts.joinToString(" · ")

    Column {
        // Month header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .background(PrimaryContainerLight)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment  = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = month,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnBackgroundLight
                )
                if (summary.isNotBlank()) {
                    Text(
                        text  = summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnBackgroundLight.copy(alpha = 0.6f)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = "${entries.size} event${if (entries.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnBackgroundLight.copy(alpha = 0.55f)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint               = OnBackgroundLight.copy(alpha = 0.55f),
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        // Entry cards, animated
        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier            = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entries.forEach { log ->
                    LogEntryCard(log = log)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Individual log entry card ────────────────────────────────────────────────

@Composable
private fun LogEntryCard(log: UsageLog) {
    val style = eventStyle(log.eventType)
    val deltaStr = when {
        log.quantityDelta > 0 -> "+${log.quantityDelta}"
        else                  -> "${log.quantityDelta}"   // already negative
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colour dot with event icon
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(style.color.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = style.icon,
                    contentDescription = style.label,
                    tint               = style.color,
                    modifier           = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Material name + category + event label
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = style.label,
                        style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color    = style.color,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(style.color.copy(alpha = 0.13f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text     = log.category,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = OnBackgroundLight.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text     = log.materialName,
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color    = OnBackgroundLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = log.timestamp.toEntryDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnBackgroundLight.copy(alpha = 0.45f)
                )
            }

            Spacer(Modifier.width(10.dp))

            // Delta badge
            Box(
                modifier         = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(style.color.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = deltaStr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    ),
                    color = style.color
                )
            }
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyJournalState(filtered: Boolean) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            imageVector       = Icons.Filled.MenuBook,
            contentDescription = null,
            modifier          = Modifier.size(72.dp),
            tint              = PrimaryLight.copy(alpha = 0.45f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = if (filtered) "No entries for this filter" else "Your journal is empty",
            style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color     = OnBackgroundLight,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = if (filtered)
                "Try selecting a different event type above."
            else
                "Start tracking by adding or editing materials in your inventory.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = OnBackgroundLight.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}
