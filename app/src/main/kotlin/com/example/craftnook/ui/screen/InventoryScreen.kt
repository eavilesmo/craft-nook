package com.example.craftnook.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.craftnook.R
import com.example.craftnook.data.repository.ArtMaterial
import com.example.craftnook.ui.viewmodel.BackupResult
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import com.example.craftnook.ui.viewmodel.StockFilter
import androidx.compose.foundation.lazy.grid.items as gridItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val materials by viewModel.allMaterials.collectAsState()
    val filteredMaterials by viewModel.filteredMaterials.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedStockFilter by viewModel.selectedStockFilter.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val backupResult by viewModel.backupResult.collectAsState()

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var fabPressed by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

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

    var selectedMaterial by remember { mutableStateOf<ArtMaterial?>(null) }

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
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            FilterDropdownBar(
                categories = availableCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                selectedStockFilter = selectedStockFilter,
                onStockFilterSelected = { viewModel.selectStockFilter(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

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
            onAdd = { name, brand, quantity, category, unit, imageUri ->
                quantity.toIntOrNull()?.let { quantityInt ->
                    viewModel.addMaterial(name, brand, quantityInt, category, unit, imageUri)
                }
                showAddMaterialDialog = false
                fabPressed = false
                pendingAddPhotoUri = null
            }
        )
    }

    if (showManageCategoriesDialog) {
        ManageCategoriesDialog(
            categories     = availableCategories,
            onAddCategory  = { viewModel.addCategory(it) },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onDismiss      = { showManageCategoriesDialog = false }
        )
    }

    if (showBackupDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
        )
    }
}

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
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedContainerColor   = Color(0xFFFFFFFF),
            unfocusedBorderColor    = Color(0xFFD7CCC8),
            focusedBorderColor      = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdownBar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedStockFilter: StockFilter,
    onStockFilterSelected: (StockFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val anchorBg     = Color(0xFFFFF3E0)
    val anchorBorder = Color(0xFFD7CCC8)
    val anchorShape  = RoundedCornerShape(14.dp)
    val labelColor   = Color(0xFF5D4037)

    val stockOptions = listOf(
        StockFilter.ALL      to "All",
        StockFilter.IN_STOCK to "In Stock",
        StockFilter.FINISHED to "Finished"
    )
    val stockLabel = stockOptions.first { it.first == selectedStockFilter }.second

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NookDropdown(
            label = if (selectedCategory == "All") "Category" else selectedCategory,
            anchorBg = anchorBg,
            anchorBorder = anchorBorder,
            anchorShape = anchorShape,
            labelColor = labelColor,
            modifier = Modifier.weight(1f)
        ) { closeMenu ->
            val sortedCategories = (listOf("All") + categories.sorted())
            sortedCategories.forEach { cat ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal,
                            color = if (cat == selectedCategory)
                                MaterialTheme.colorScheme.primary else labelColor
                        )
                    },
                    onClick = {
                        onCategorySelected(cat)
                        closeMenu()
                    }
                )
            }
        }

        NookDropdown(
            label = stockLabel,
            anchorBg = anchorBg,
            anchorBorder = anchorBorder,
            anchorShape = anchorShape,
            labelColor = labelColor,
            modifier = Modifier.weight(1f)
        ) { closeMenu ->
            stockOptions.forEach { (filter, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (filter == selectedStockFilter) FontWeight.Bold else FontWeight.Normal,
                            color = if (filter == selectedStockFilter)
                                MaterialTheme.colorScheme.primary else labelColor
                        )
                    },
                    onClick = {
                        onStockFilterSelected(filter)
                        closeMenu()
                    }
                )
            }
        }
    }
}

@Composable
private fun NookDropdown(
    label: String,
    anchorBg: Color,
    anchorBorder: Color,
    anchorShape: RoundedCornerShape,
    labelColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable (closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(anchorShape)
                .background(anchorBg)
                .border(1.dp, anchorBorder, anchorShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = if (expanded) 180f else 0f }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            content { expanded = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryTopAppBar(
    onManageCategories: () -> Unit,
    onBackupRestore: () -> Unit
) {
    var showSettingsMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter            = painterResource(R.drawable.app_logo),
                    contentDescription = "Craft Nook logo",
                    modifier           = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(
                    text       = "Craft Nook",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
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
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        gridItems(
            items = materials,
            key = { material -> material.id }
        ) { material ->
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

@OptIn(ExperimentalFoundationApi::class)
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
    var actionsVisible by remember { mutableStateOf(false) }

    val cardShape  = RoundedCornerShape(16.dp)
    val photoShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val context    = LocalContext.current
    val isFinished = material.quantity == 0

    val footerBg       = Color(0xFFEDE0D4)
    val footerTextMain = Color(0xFF4E342E)
    val pillBg         = Color(0xFFBCAAA4).copy(alpha = 0.40f)
    val pillText       = Color(0xFF4E342E)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isFinished) 0.42f else 1f)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    actionsVisible = false
                    onClick()
                },
                onLongClick = { actionsVisible = !actionsVisible }
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFinished) 0.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isFinished) Color(0xFFD7CCC8).copy(alpha = 0.45f) else Color(0xFFD7CCC8)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(5f / 4f)
                    .clip(photoShape)
            ) {
                if (!material.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(material.photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = material.name,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        colorFilter = if (isFinished)
                            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isFinished) Color(0xFFEEEEEE) else Color(0xFFF5F0EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (isFinished) Color(0xFFBDBDBD) else Color(0xFFBCAAA4)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(footerBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = footerTextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(pillBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${material.quantity} ${material.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = pillText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { showEditDialog = true },
                            shape = RoundedCornerShape(7.dp),
                            color = Color(0xFFF3E5D8),
                            tonalElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(13.dp),
                                    tint = Color(0xFF5D4037)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { showDeleteConfirmation = true },
                            shape = RoundedCornerShape(7.dp),
                            color = Color(0xFFF3E5D8),
                            tonalElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(13.dp),
                                    tint = Color(0xFFBF360C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showEditDialog) {
        EditMaterialDialog(
            material = material,
            categories = categories,
            photoUri = pendingPhotoUri ?: material.photoUri,
            onPickPhoto = onPickPhoto,
            onDismiss = {
                showEditDialog = false
                actionsVisible = false
                onPhotoUriConsumed()
            },
            onSave = { updatedMaterial ->
                onEdit(updatedMaterial)
                showEditDialog = false
                actionsVisible = false
                onPhotoUriConsumed()
            }
        )
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    categories: List<String>,
    photoUri: String?,
    onPickPhoto: () -> Unit,
    onDismiss: () -> Unit,
    onAdd: (name: String, brand: String, quantity: String, category: String, unit: String, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("unit") }
    val context = LocalContext.current

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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Acrylic Paint") }
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Faber-Castell") }
                )

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

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 10") }
                )

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
                onClick = { onAdd(name, brand, quantity, selectedCategory, unit, photoUri) },
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

                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

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


