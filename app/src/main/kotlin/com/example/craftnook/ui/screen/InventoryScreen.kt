package com.example.craftnook.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import coil.transform.RoundedCornersTransformation
import com.example.craftnook.data.repository.ArtMaterial
import com.example.craftnook.ui.theme.CategoryA4NotebooksColor
import com.example.craftnook.ui.theme.CategoryA5NotebooksColor
import com.example.craftnook.ui.theme.CategoryAlcoholMarkersColor
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
import com.example.craftnook.ui.theme.OutlineLight
import com.example.craftnook.ui.viewmodel.BackupResult
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import com.example.craftnook.ui.viewmodel.PendingQuantityConfirmation
import kotlinx.coroutines.launch

/**
 * Main Inventory Screen
 * Displays a list of art materials with their details and stock status
 * Includes search and category filtering
 * Materials are clickable to view details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val materials by viewModel.allMaterials.collectAsState()
    val filteredMaterials by viewModel.filteredMaterials.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val pendingConfirmation by viewModel.pendingQuantityConfirmation.collectAsState()
    val backupResult by viewModel.backupResult.collectAsState()

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var fabPressed by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show a snackbar whenever a backup result arrives
    LaunchedEffect(backupResult) {
        val result = backupResult ?: return@LaunchedEffect
        val message = when (result) {
            is BackupResult.ExportSuccess    -> "Backup exported successfully."
            is BackupResult.ImportSuccess    ->
                "Import complete: ${result.materialsAdded} materials, ${result.logsAdded} journal entries added."
            is BackupResult.Failure          -> "Error: ${result.message}"
        }
        snackbarHostState.showSnackbar(message)
        viewModel.clearBackupResult()
    }

    // Material details bottom sheet state
    var selectedMaterial by remember { mutableStateOf<ArtMaterial?>(null) }

    // ── Photo picker state ────────────────────────────────────────────────
    // Pending URI is written by the picker launcher and consumed by whichever
    // dialog is currently open. Using separate callbacks avoids cross-dialog
    // state leakage.
    var pendingAddPhotoUri by remember { mutableStateOf<String?>(null) }
    var pendingEditPhotoUri by remember { mutableStateOf<String?>(null) }

    val addPhotoPickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
        pendingAddPhotoUri = uri?.toString()
    }
    val editPhotoPickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
        pendingEditPhotoUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                totalItems = materials.size,
                onManageCategories = { showManageCategoriesDialog = true },
                onBackupRestore    = { showBackupDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    fabPressed = true
                    showAddMaterialDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.graphicsLayer {
                    scaleX = if (fabPressed) 0.95f else 1f
                    scaleY = if (fabPressed) 0.95f else 1f
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add new material"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Category filter chips
            CategoryFilterRow(
                categories = availableCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Materials list
            if (filteredMaterials.isEmpty()) {
                if (materials.isEmpty()) {
                    EmptyInventoryState(modifier = Modifier.weight(1f))
                } else {
                    EmptySearchState(modifier = Modifier.weight(1f))
                }
            } else {
                MaterialsList(
                    materials = filteredMaterials,
                    categories = availableCategories,
                    viewModel = viewModel,
                    onPickEditPhoto = { editPhotoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                    pendingEditPhotoUri = pendingEditPhotoUri,
                    onEditPhotoUriConsumed = { pendingEditPhotoUri = null },
                    onMaterialClick = { material ->
                        selectedMaterial = material
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 32.dp)
                )
            }
        }
    }

    // Material details dialog (AlertDialog instead of bottom sheet)
    if (selectedMaterial != null) {
        MaterialDetailsDialog(
            material = selectedMaterial!!,
            onDismiss = {
                selectedMaterial = null
            }
        )
    }

    if (showAddMaterialDialog) {
        AddMaterialDialog(
            categories = availableCategories,
            photoUri = pendingAddPhotoUri,
            onPickPhoto = {
                addPhotoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            onDismiss = {
                showAddMaterialDialog = false
                fabPressed = false
                pendingAddPhotoUri = null
            },
            onAdd = { name, brand, quantity, category, imageUri ->
                quantity.toIntOrNull()?.let { quantityInt ->
                    viewModel.addMaterial(name, brand, quantityInt, category, imageUri)
                }
                showAddMaterialDialog = false
                fabPressed = false
                pendingAddPhotoUri = null
            }
        )
    }

    // "Used it / Just correcting?" dialog — shown when a quantity is reduced during edit
    if (pendingConfirmation != null) {
        QuantityReductionDialog(
            confirmation = pendingConfirmation!!,
            onUsed       = { viewModel.confirmQuantityChange(wasUsed = true) },
            onCorrection = { viewModel.confirmQuantityChange(wasUsed = false) },
            onDismiss    = { viewModel.dismissQuantityConfirmation() }
        )
    }

    // Manage Categories dialog
    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            categories     = availableCategories,
            onAddCategory  = { viewModel.addCategory(it) },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onDismiss      = { showManageCategoriesDialog = false }
        )
    }

    // Backup & Restore dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
        )
    }
}

/**
 * Search Bar
 * Material 3 TextField for real-time search filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search materials...") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Category Filter Row
 * Row of FilterChips for category filtering with smooth selection animation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCategories = listOf("All") + categories
    
    // Function to get pastel color for category
    fun getCategoryColor(category: String): Color = when (category) {
        "Paint" -> CategoryPaintColor
        "Brushes" -> CategoryBrushesColor
        "Paper" -> CategoryPaperColor
        "Pens" -> CategoryPensColor
        "Alcohol Markers" -> CategoryAlcoholMarkersColor
        "Water-based Markers" -> CategoryWaterbasedMarkersColor
        "Colored Pencils" -> CategoryColoredPencilsColor
        "Drawing Pencils" -> CategoryDrawingPencilsColor
        "Mechanical Pencil Leads" -> CategoryMechanicalPencilLeadsColor
        "Mechanical Pencils" -> CategoryMechanicalPencilsColor
        "White Pens" -> CategoryWhitePensColor
        "Glitter Pens" -> CategoryGlitterPensColor
        "Metallic Pens" -> CategoryMetallicPensColor
        "Crayons" -> CategoryCrayonsColor
        "Highlighters" -> CategoryHighlightersColor
        "A4 Notebooks" -> CategoryA4NotebooksColor
        "A5 Notebooks" -> CategoryA5NotebooksColor
        "Fountain Pen Cartridges" -> CategoryFountainPenCartridgesColor
        "Fineliners" -> CategoryFinelinersColor
        "All" -> Color(0xFFE8E8E8) // Light gray for "All"
        else -> Color(0xFFE0E0E0) // Default light gray
    }

    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(allCategories) { category ->
            AnimatedContent(
                targetState = selectedCategory == category,
                label = "filterChipAnimation"
            ) { isSelected ->
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (isSelected) 1.0f else 0.95f
                        scaleY = if (isSelected) 1.0f else 0.95f
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = getCategoryColor(category),
                        labelColor = Color(0xFF3F3F3F),
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

/**
 * Top app bar for inventory screen
 * Shows title, total inventory count, and analytics toggle button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryTopAppBar(
    totalItems: Int,
    onManageCategories: () -> Unit,
    onBackupRestore: () -> Unit
) {
    var showSettingsMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "Craft Nook",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            Box {
                IconButton(onClick = { showSettingsMenu = true }) {
                    Icon(
                        imageVector        = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint               = MaterialTheme.colorScheme.onPrimary
                    )
                }
                DropdownMenu(
                    expanded         = showSettingsMenu,
                    onDismissRequest = { showSettingsMenu = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("Manage Categories") },
                        leadingIcon = {
                            Icon(Icons.Filled.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showSettingsMenu = false
                            onManageCategories()
                        }
                    )
                    DropdownMenuItem(
                        text    = { Text("Backup & Restore") },
                        leadingIcon = {
                            Icon(Icons.Filled.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        onClick = {
                            showSettingsMenu = false
                            onBackupRestore()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Materials Grid
 * Displays all materials in a 2-column LazyVerticalGrid with airy spacing
 * Includes staggered fade-in animations for smooth visual feedback
 */
@Composable
private fun MaterialsList(
    materials: List<ArtMaterial>,
    categories: List<String>,
    viewModel: InventoryViewModel,
    onPickEditPhoto: () -> Unit,
    pendingEditPhotoUri: String?,
    onEditPhotoUriConsumed: () -> Unit,
    onMaterialClick: (ArtMaterial) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        gridItems(
            items = materials,
            key = { material -> material.id }
        ) { material ->
            // Staggered fade-in animation for each item
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInVertically(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = { 40 }
                )
            ) {
                InventoryItemCard(
                    material = material,
                    categories = categories,
                    onEdit = { updatedMaterial -> viewModel.updateMaterial(updatedMaterial) },
                    onDelete = { viewModel.deleteMaterial(material.id) },
                    onClick = { onMaterialClick(material) },
                    onPickPhoto = onPickEditPhoto,
                    pendingPhotoUri = pendingEditPhotoUri,
                    onPhotoUriConsumed = onEditPhotoUriConsumed
                )
            }
        }
    }
}

/**
 * InventoryItemCard — photo-first grid cell.
 *
 * Layout (1:1 aspect ratio card):
 *   ┌──────────────────────────┐
 *   │  Photo area  80%         │  ← AsyncImage or leaf placeholder
 *   │  [Edit] overlay          │    rounded top corners (24dp)
 *   │             [Delete]     │    action buttons sit on bottom edge of photo
 *   ├──────────────────────────┤
 *   │ Name (bold)  │ Category  │  ← 20% info bar
 *   │              │ Qty       │
 *   └──────────────────────────┘
 */
@Composable
private fun InventoryItemCard(
    material: ArtMaterial,
    categories: List<String>,
    onEdit: (ArtMaterial) -> Unit = {},
    onDelete: () -> Unit = {},
    onClick: () -> Unit = {},
    onPickPhoto: () -> Unit = {},
    pendingPhotoUri: String? = null,
    onPhotoUriConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(20.dp)
    val photoShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val context = LocalContext.current

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                enabled = true,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7CCC8))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Photo area (80%) ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.80f)
                    .clip(photoShape)
            ) {
                if (!material.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(material.photoUri)
                            .crossfade(true)
                            .transformations(RoundedCornersTransformation(topLeft = 20f, topRight = 20f))
                            .build(),
                        contentDescription = material.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder — subtle warm background + leaf icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F0EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFFBCAAA4)
                        )
                    }
                }

                // Edit button — bottom-left overlay on photo
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .size(28.dp)
                        .clickable { showEditDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit material",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Delete button — bottom-right overlay on photo
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clickable { showDeleteConfirmation = true },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete material",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── Info bar (20%) ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: material name
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Right: category icon + quantity stacked
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(material.category),
                        contentDescription = material.category,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF8D6E63)
                    )
                    Text(
                        text = "${material.quantity} ${material.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog) {
        EditMaterialDialog(
            material = material,
            categories = categories,
            photoUri = pendingPhotoUri ?: material.photoUri,
            onPickPhoto = onPickPhoto,
            onDismiss = {
                showEditDialog = false
                onPhotoUriConsumed()
            },
            onSave = { updatedMaterial ->
                onEdit(updatedMaterial)
                showEditDialog = false
                onPhotoUriConsumed()
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Material") },
            text = { Text("Are you sure you want to delete \"${material.name}\"? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirmation = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Maps category names to Material Icons
 */
private fun getCategoryIcon(category: String) = when (category) {
    "Paint" -> Icons.Filled.Palette
    "Brushes" -> Icons.Filled.Brush
    "Paper" -> Icons.Filled.Note
    "Pens" -> Icons.Filled.Edit
    "Alcohol Markers" -> Icons.Filled.Edit
    "Water-based Markers" -> Icons.Filled.Edit
    "Colored Pencils" -> Icons.Filled.Edit
    "Drawing Pencils" -> Icons.Filled.Edit
    "Mechanical Pencil Leads" -> Icons.Filled.Edit
    "Mechanical Pencils" -> Icons.Filled.Edit
    "White Pens" -> Icons.Filled.Edit
    "Glitter Pens" -> Icons.Filled.Edit
    "Metallic Pens" -> Icons.Filled.Edit
    "Crayons" -> Icons.Filled.Edit
    "Highlighters" -> Icons.Filled.Highlight
    "A4 Notebooks" -> Icons.Filled.Note
    "A5 Notebooks" -> Icons.Filled.Note
    "Fountain Pen Cartridges" -> Icons.Filled.Edit
    "Fineliners" -> Icons.Filled.Edit
    "Erasers" -> Icons.Filled.Edit
    else -> Icons.Filled.Palette
}

/**
 * Category Badge
 * Shows the category of the material in a styled chip with premium pastel colors.
 * In compact mode (used in grid cards) shows only the icon to save space.
 */
@Composable
private fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var isHovered by remember { mutableStateOf(false) }
    
    // Map categories to premium pastel colors
    val backgroundColor = when (category) {
        "Paint" -> CategoryPaintColor
        "Brushes" -> CategoryBrushesColor
        "Paper" -> CategoryPaperColor
        "Pens" -> CategoryPensColor
        "Alcohol Markers" -> CategoryAlcoholMarkersColor
        "Water-based Markers" -> CategoryWaterbasedMarkersColor
        "Colored Pencils" -> CategoryColoredPencilsColor
        "Drawing Pencils" -> CategoryDrawingPencilsColor
        "Mechanical Pencil Leads" -> CategoryMechanicalPencilLeadsColor
        "Mechanical Pencils" -> CategoryMechanicalPencilsColor
        "White Pens" -> CategoryWhitePensColor
        "Glitter Pens" -> CategoryGlitterPensColor
        "Metallic Pens" -> CategoryMetallicPensColor
        "Crayons" -> CategoryCrayonsColor
        "Highlighters" -> CategoryHighlightersColor
        "A4 Notebooks" -> CategoryA4NotebooksColor
        "A5 Notebooks" -> CategoryA5NotebooksColor
        "Fountain Pen Cartridges" -> CategoryFountainPenCartridgesColor
        "Fineliners" -> CategoryFinelinersColor
        "Erasers" -> CategoryErasersColor
        else -> CategoryPaintColor
    }    
    // Dark text for good contrast on pastels
    val textColor = Color(0xFF2D3748)
    val iconTint = Color(0xFF795548) // Warm brown — natural, cozy

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(18.dp)
            )
            .border(1.dp, OutlineLight.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(
                horizontal = if (compact) 8.dp else 16.dp,
                vertical = if (compact) 6.dp else 10.dp
            )
            .graphicsLayer {
                scaleX = if (isHovered) 1.05f else 1f
                scaleY = if (isHovered) 1.05f else 1f
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = category,
                modifier = Modifier.size(if (compact) 13.dp else 14.dp),
                tint = iconTint
            )
            if (!compact) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

/**
 * Quantity Info
 * Displays stock quantity with icon
 */
@Composable
private fun QuantityInfo(
    quantity: Int,
    unit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$quantity $unit",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Empty Inventory State
 * Displayed when there are no materials in inventory at all.
 * Features fade and scale animation on appearance.
 */
@Composable
private fun EmptyInventoryState(
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            initialScale = 0.95f
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF81C784)
                )
                Text(
                    text = "It's quiet here...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add your first material to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Empty Search/Filter State
 * Displayed when a search query or category filter returns no results.
 * Features fade and scale animation on appearance.
 */
@Composable
private fun EmptySearchState(
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            initialScale = 0.95f
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF81C784)
                )
                Text(
                    text = "It's quiet here...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Try a different search or category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Add Material Dialog
 * Material 3 dialog for adding new materials to the inventory
 * Includes validation for Name and Quantity fields
 * Allows category selection from fixed dropdown
 * Allows gallery photo selection via system photo picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    categories: List<String>,
    photoUri: String?,
    onPickPhoto: () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (name: String, brand: String, quantity: String, category: String, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Validation: name and quantity must be non-empty, category must be selected
    val isValid = name.isNotBlank() && quantity.isNotBlank() && quantity.toIntOrNull() != null && quantity.toIntOrNull()!! > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .graphicsLayer {
                alpha = 1f
            },
        title = {
            Text("Add New Material")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Add Photo button ──────────────────────────────────────
                OutlinedButton(
                    onClick = onPickPhoto,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (photoUri != null) "Photo selected" else "Add Photo",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Thumbnail preview if a photo was picked
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUri)
                            .crossfade(true)
                            .transformations(RoundedCornersTransformation(16f))
                            .build(),
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Acrylic Paint") }
                )

                // Brand field
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Faber-Castell") }
                )

                // Category dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { categoryDropdownExpanded = !categoryDropdownExpanded }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quantity field
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 10") }
                )

                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, brand, quantity, selectedCategory, photoUri) },
                enabled = isValid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Edit Material Dialog
 * Material 3 dialog for editing existing materials in the inventory
 * Uses a numeric stepper for quantity and proper dropdowns for category/unit
 * Allows gallery photo selection via system photo picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMaterialDialog(
    material: ArtMaterial,
    categories: List<String>,
    photoUri: String?,
    onPickPhoto: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (ArtMaterial) -> Unit
) {
    var name by remember { mutableStateOf(material.name) }
    var brand by remember { mutableStateOf(material.description) }
    var quantity by remember { mutableStateOf(material.quantity) }
    var category by remember { mutableStateOf(material.category) }
    var unit by remember { mutableStateOf(material.unit) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // photoUri from the parent (picker result) takes priority over the stored one
    val displayPhotoUri = photoUri ?: material.photoUri

    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Material")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Add / Change Photo button ─────────────────────────────
                OutlinedButton(
                    onClick = onPickPhoto,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!displayPhotoUri.isNullOrBlank()) "Change Photo" else "Add Photo",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Thumbnail preview
                if (!displayPhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(displayPhotoUri)
                            .crossfade(true)
                            .transformations(RoundedCornersTransformation(16f))
                            .build(),
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand/Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { categoryDropdownExpanded = !categoryDropdownExpanded }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quantity Stepper
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quantity *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Minus button
                        IconButton(
                            onClick = { quantity = (quantity - 1).coerceAtLeast(0) },
                            modifier = Modifier
                                .weight(0.2f)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease quantity",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Quantity display
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Plus button
                        IconButton(
                            onClick = { quantity += 1 },
                            modifier = Modifier
                                .weight(0.2f)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase quantity",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., bottle, tube, sheet") }
                )

                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedMaterial = material.copy(
                        name = name,
                        description = brand,
                        quantity = quantity,
                        category = category,
                        unit = unit,
                        photoUri = displayPhotoUri
                    )
                    onSave(updatedMaterial)
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Material Details Dialog
 * Displays material information in a popup dialog instead of a bottom sheet
 */
@Composable
private fun MaterialDetailsDialog(
    material: ArtMaterial,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = material.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Description
                if (material.description.isNotBlank()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = material.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Current Stock
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Current Stock",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${material.quantity} ${material.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }

                // Category
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = material.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Unit
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Unit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = material.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Dialog shown when the user saves an edit that reduces quantity.
 * Asks whether the reduction represents real usage or a data correction.
 */
@Composable
private fun QuantityReductionDialog(
    confirmation: PendingQuantityConfirmation,
    onUsed: () -> Unit,
    onCorrection: () -> Unit,
    onDismiss: () -> Unit
) {
    val delta = confirmation.oldQuantity - confirmation.newQuantity  // positive number
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quantity reduced") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "\"${confirmation.material.name}\" went from " +
                           "${confirmation.oldQuantity} → ${confirmation.newQuantity} " +
                           "(-$delta ${confirmation.material.unit}).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Did you use this material, or are you correcting a mistake?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(onClick = onUsed) {
                Text("Used it")
            }
        },
        dismissButton = {
            TextButton(onClick = onCorrection) {
                Text("Just correcting")
            }
        }
    )
}
