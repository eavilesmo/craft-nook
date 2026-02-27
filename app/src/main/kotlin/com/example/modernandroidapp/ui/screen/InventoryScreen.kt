package com.example.modernandroidapp.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import com.example.modernandroidapp.data.repository.ArtMaterial
import com.example.modernandroidapp.ui.viewmodel.InventoryViewModel
import com.example.modernandroidapp.ui.theme.CategoryPaintColor
import com.example.modernandroidapp.ui.theme.CategoryBrushesColor
import com.example.modernandroidapp.ui.theme.CategoryCanvasColor
import com.example.modernandroidapp.ui.theme.CategoryPaperColor
import com.example.modernandroidapp.ui.theme.CategoryPencilsColor
import com.example.modernandroidapp.ui.theme.CategoryMarkersColor
import com.example.modernandroidapp.ui.theme.CategorySketchbooksColor
import com.example.modernandroidapp.ui.theme.CategoryOtherColor

/**
 * Main Inventory Screen
 * Displays a list of art materials with their details and stock status
 * Includes search and category filtering
 */
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val materials by viewModel.allMaterials.collectAsState()
    val filteredMaterials by viewModel.filteredMaterials.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var fabPressed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                totalItems = materials.size
            )
        },
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Category filter chips
            CategoryFilterRow(
                categories = availableCategories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }

    if (showAddMaterialDialog) {
        AddMaterialDialog(
            categories = InventoryViewModel.FIXED_CATEGORIES,
            onDismiss = { 
                showAddMaterialDialog = false
                fabPressed = false
            },
            onAdd = { name, brand, quantity, category ->
                quantity.toIntOrNull()?.let { quantityInt ->
                    viewModel.addMaterial(name, brand, quantityInt, category)
                }
                showAddMaterialDialog = false
                fabPressed = false
            }
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
        "Canvas" -> CategoryCanvasColor
        "Paper" -> CategoryPaperColor
        "Pencils" -> CategoryPencilsColor
        "Markers" -> CategoryMarkersColor
        "Sketchbooks" -> CategorySketchbooksColor
        "All" -> Color(0xFFE8E8E8) // Light gray for "All"
        else -> CategoryOtherColor
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
 * Shows title and total inventory count
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryTopAppBar(
    totalItems: Int
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Craft Nook",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$totalItems materials",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Materials List
 * Displays all materials in a LazyColumn with proper spacing
 * Includes staggered fade-in animations for smooth visual feedback
 */
@Composable
private fun MaterialsList(
    materials: List<ArtMaterial>,
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
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
                    onEdit = { updatedMaterial -> viewModel.updateMaterial(updatedMaterial) },
                    onDelete = { viewModel.deleteMaterial(material.id) }
                )
            }
        }
    }
}

/**
 * Inventory Item Card
 * Shows individual art material details with edit and delete buttons
 * Includes scale and fade animations on button press
 *
 * Uses Material 3 Card component
 */
@Composable
private fun InventoryItemCard(
    material: ArtMaterial,
    onEdit: (ArtMaterial) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editButtonPressed by remember { mutableStateOf(false) }
    var deleteButtonPressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = androidx.compose.material3.CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header row with name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Material Name - Now Bigger & More Prominent
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = material.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    if (material.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = material.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Action buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button with press animation
                    Button(
                        onClick = { 
                            editButtonPressed = true
                            showEditDialog = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = if (editButtonPressed) 0.95f else 1f
                                scaleY = if (editButtonPressed) 0.95f else 1f
                            },
                        shape = RoundedCornerShape(6.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit material",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete button - Outlined version with subtle styling and press animation
                    Button(
                        onClick = { 
                            deleteButtonPressed = true
                            showDeleteConfirmation = true
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.5.dp, Color(0xFFCBD5E0), RoundedCornerShape(8.dp))
                            .graphicsLayer {
                                scaleX = if (deleteButtonPressed) 0.95f else 1f
                                scaleY = if (deleteButtonPressed) 0.95f else 1f
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF718096)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete material",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details row with category and quantity
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Badge with scale animation on hover
                CategoryBadge(category = material.category)

                Spacer(modifier = Modifier.width(12.dp))

                // Quantity and Unit
                QuantityInfo(
                    quantity = material.quantity,
                    unit = material.unit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Edit dialog with fade and scale animation
    if (showEditDialog) {
        EditMaterialDialog(
            material = material,
            onDismiss = { 
                showEditDialog = false
                editButtonPressed = false
            },
            onSave = { updatedMaterial ->
                onEdit(updatedMaterial)
                showEditDialog = false
                editButtonPressed = false
            }
        )
    }

    // Delete confirmation dialog with fade and scale animation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                deleteButtonPressed = false
            },
            title = {
                Text("Delete Material")
            },
            text = {
                Text("Are you sure you want to delete \"${material.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                        deleteButtonPressed = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirmation = false
                    deleteButtonPressed = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Category Badge
 * Shows the category of the material in a styled chip with premium pastel colors
 * Includes subtle scale animation on interaction
 */
@Composable
private fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    
    // Map categories to premium pastel colors
    val backgroundColor = when (category) {
        "Paint" -> CategoryPaintColor
        "Brushes" -> CategoryBrushesColor
        "Canvas" -> CategoryCanvasColor
        "Paper" -> CategoryPaperColor
        "Pencils" -> CategoryPencilsColor
        "Markers" -> CategoryMarkersColor
        "Sketchbooks" -> CategorySketchbooksColor
        else -> CategoryOtherColor
    }
    
    // Dark text for good contrast on pastels
    val textColor = Color(0xFF2D3748)

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .graphicsLayer {
                scaleX = if (isHovered) 1.05f else 1f
                scaleY = if (isHovered) 1.05f else 1f
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
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
        Icon(
            imageVector = Icons.Filled.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
 * Displayed when there are no materials in inventory
 * Features fade and scale animation on appearance
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No Materials",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your inventory is empty. Start by adding materials.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Empty Search State
 * Displayed when search or filter returns no results
 * Features fade and scale animation on appearance
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No Results",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "No materials match your search or filter criteria.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
 * Features smooth fade-in and scale animation on dialog appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (name: String, brand: String, quantity: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

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
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                    
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }

                    // Invisible button to open dropdown
                    Button(
                        onClick = { categoryDropdownExpanded = true },
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {}
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
                onClick = { onAdd(name, brand, quantity, selectedCategory) },
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
 * Allows updating name, brand, quantity, category, unit, and price
 * Features smooth fade-in and scale animation on dialog appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMaterialDialog(
    material: ArtMaterial,
    onDismiss: () -> Unit,
    onSave: (ArtMaterial) -> Unit
) {
    var name by remember { mutableStateOf(material.name) }
    var brand by remember { mutableStateOf(material.description) }
    var quantity by remember { mutableStateOf(material.quantity.toString()) }
    var category by remember { mutableStateOf(material.category) }
    var unit by remember { mutableStateOf(material.unit) }
    var price by remember { mutableStateOf(material.price.toString()) }

    // Validation: name and quantity must be valid
    val isValid = name.isNotBlank() && quantity.toIntOrNull() != null && quantity.toIntOrNull()!! > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .graphicsLayer {
                alpha = 1f
            },
        title = {
            Text("Edit Material")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                        quantity = quantity.toIntOrNull() ?: material.quantity,
                        category = category,
                        unit = unit,
                        price = price.toDoubleOrNull() ?: material.price
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
