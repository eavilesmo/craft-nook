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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

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

// Wood-brown colour used for the per-entry delete icon.
// Matches the app's OnBackgroundLight cocoa tone but slightly lighter/muted.
private val WoodBrown = Color(0xFF8D6E63)

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: InventoryViewModel) {
    val allLogs         by viewModel.logEntries.collectAsState()
    var activeFilter    by remember { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    val filteredLogs = if (activeFilter == "All") allLogs
                       else allLogs.filter { it.eventType == activeFilter }

    // Group by month, preserving newest-first order
    val byMonth: Map<String, List<UsageLog>> = filteredLogs
        .groupBy { it.timestamp.toMonthKey() }

    // Collect single-entry deletions and show an Undo snackbar.
    // Re-inserting via undoDeleteJournalEntry does not touch inventory quantities.
    LaunchedEffect(Unit) {
        viewModel.deletedLogEntry.collect { deleted ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message        = "Entry removed",
                    actionLabel    = "Undo",
                    duration       = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDeleteJournalEntry(deleted)
                }
            }
        }
    }

    // ── Clear history confirmation dialog ────────────────────────────────────
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor   = Color.White,
            shape            = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text  = "Clear history",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnBackgroundLight
                )
            },
            text = {
                Text(
                    text  = "Are you sure you want to clear your history? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnBackgroundLight.copy(alpha = 0.75f)
                )
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(
                        text  = "Cancel",
                        color = OnBackgroundLight.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearJournal()
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text  = "Clear Everything",
                        color = ErrorLight,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Usage Journal",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector        = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear journal",
                            tint               = OnBackgroundLight.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = PrimaryContainerLight,
                    titleContentColor = OnBackgroundLight
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData    = data,
                    shape           = RoundedCornerShape(12.dp),
                    containerColor  = OnBackgroundLight,
                    contentColor    = Color.White,
                    actionColor     = PrimaryLight
                )
            }
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
                            MonthSection(
                                month    = month,
                                entries  = entries,
                                onDelete = { log -> viewModel.deleteJournalEntry(log) }
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Summary badge colours ────────────────────────────────────────────────────

private val SummaryGreenText = Color(0xFF2E7D32)       // Dark green text
private val SummaryGreenBg   = Color(0xFFE8F5E9)       // Faint green pill background
private val SummaryRedText   = Color(0xFFC62828)        // Strong red text
private val SummaryRedBg     = Color(0xFFFFEBEE)        // Faint red pill background
private val SummaryGrayText  = Color(0xFF6D4C41)        // Muted brown for deleted
private val SummaryGrayBg    = Color(0xFFF3E5F5)        // Faint purple-grey for deleted

@Composable
private fun SummaryBadge(count: Int, label: String, textColor: Color, bgColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text       = count.toString(),
            style      = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color      = textColor,
            fontSize   = 11.sp
        )
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color      = textColor,
            fontSize   = 11.sp
        )
    }
}

// ── Month section (collapsible) ──────────────────────────────────────────────

@Composable
private fun MonthSection(
    month:    String,
    entries:  List<UsageLog>,
    onDelete: (UsageLog) -> Unit
) {
    var expanded by remember(month) { mutableStateOf(true) }

    val added     = entries.count { it.eventType == EventType.ADDED }
    val restocked = entries.count { it.eventType == EventType.RESTOCKED }
    val used      = entries.count { it.eventType == EventType.USED }
    val deleted   = entries.count { it.eventType == EventType.DELETED }

    val hasSummary = added > 0 || restocked > 0 || used > 0 || deleted > 0

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
                if (hasSummary) {
                    Spacer(Modifier.height(5.dp))
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Green group: added + restocked
                        if (added > 0) SummaryBadge(added, "added", SummaryGreenText, SummaryGreenBg)
                        if (restocked > 0) SummaryBadge(restocked, "restocked", SummaryGreenText, SummaryGreenBg)

                        // Pipe separator between green and red groups
                        if ((added > 0 || restocked > 0) && (used > 0 || deleted > 0)) {
                            Text(
                                text  = "|",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnBackgroundLight.copy(alpha = 0.25f),
                                fontSize = 11.sp
                            )
                        }

                        // Red group: used
                        if (used > 0) SummaryBadge(used, "used", SummaryRedText, SummaryRedBg)

                        // Muted group: deleted
                        if (deleted > 0) SummaryBadge(deleted, "deleted", SummaryGrayText, SummaryGrayBg)
                    }
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
                    LogEntryCard(log = log, onDelete = { onDelete(log) })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Individual log entry card ────────────────────────────────────────────────

@Composable
private fun LogEntryCard(log: UsageLog, onDelete: () -> Unit) {
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

            Spacer(Modifier.width(4.dp))

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

            // Delete icon — subtle wood-brown trash can
            IconButton(
                onClick  = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.Delete,
                    contentDescription = "Delete entry",
                    tint               = WoodBrown.copy(alpha = 0.55f),
                    modifier           = Modifier.size(18.dp)
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
