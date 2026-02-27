package com.example.modernandroidapp.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.modernandroidapp.data.repository.ArtMaterial
import com.example.modernandroidapp.ui.viewmodel.InventoryViewModel

/**
 * Main Inventory Screen
 * Displays a list of art materials with their details and stock status
 */
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val materials by viewModel.allMaterials.collectAsState()
    val lowStockCount = materials.count { viewModel.isLowStock(it) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                totalItems = materials.size,
                lowStockCount = lowStockCount
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMaterialDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add new material"
                )
            }
        }
    ) { paddingValues ->
        if (materials.isEmpty()) {
            EmptyInventoryState(modifier = Modifier.padding(paddingValues))
        } else {
            MaterialsList(
                materials = materials,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (showAddMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showAddMaterialDialog = false },
            onAdd = { name, brand, quantity ->
                quantity.toIntOrNull()?.let { quantityInt ->
                    viewModel.addMaterial(name, brand, quantityInt)
                }
                showAddMaterialDialog = false
            }
        )
    }
}

/**
 * Top app bar for inventory screen
 * Shows title and summary of inventory status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryTopAppBar(
    totalItems: Int,
    lowStockCount: Int
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Craft Nook Inventory",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$totalItems items • $lowStockCount low stock",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
 */
@Composable
private fun MaterialsList(
    materials: List<ArtMaterial>,
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = materials,
            key = { material -> material.id }
        ) { material ->
            InventoryItemCard(
                material = material,
                isLowStock = viewModel.isLowStock(material),
                onEdit = { updatedMaterial -> viewModel.updateMaterial(updatedMaterial) },
                onDelete = { viewModel.deleteMaterial(material.id) }
            )
        }
    }
}

/**
 * Inventory Item Card
 * Shows individual art material details with low stock indicator, edit and delete buttons
 *
 * Uses Material 3 Card component with conditional styling based on stock status
 */
@Composable
private fun InventoryItemCard(
    material: ArtMaterial,
    isLowStock: Boolean,
    onEdit: (ArtMaterial) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with name and low stock indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Material Name
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = material.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = material.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Action buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .size(36.dp),
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

                    // Delete button
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .size(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete material",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Low Stock Alert Icon
                    if (isLowStock) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Low Stock Alert",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details row with category, quantity, and price
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Badge
                CategoryBadge(category = material.category)

                Spacer(modifier = Modifier.width(8.dp))

                // Quantity and Unit
                QuantityInfo(
                    quantity = material.quantity,
                    unit = material.unit,
                    minStock = material.minStock,
                    isLowStock = isLowStock,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Price
                Text(
                    text = "$${String.format("%.2f", material.price)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Edit dialog
    if (showEditDialog) {
        EditMaterialDialog(
            material = material,
            onDismiss = { showEditDialog = false },
            onSave = { updatedMaterial ->
                onEdit(updatedMaterial)
                showEditDialog = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
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
                    }
                ) {
                    Text("Delete")
                }
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
 * Category Badge
 * Shows the category of the material in a styled chip
 */
@Composable
private fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Category,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Quantity Info
 * Displays stock quantity with visual indicator for low stock status
 */
@Composable
private fun QuantityInfo(
    quantity: Int,
    unit: String,
    minStock: Int,
    isLowStock: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isLowStock)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$quantity $unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isLowStock)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Min: $minStock",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty Inventory State
 * Displayed when there are no materials in inventory
 */
@Composable
private fun EmptyInventoryState(
    modifier: Modifier = Modifier
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

/**
 * Add Material Dialog
 * Material 3 dialog for adding new materials to the inventory
 * Includes validation for Name and Quantity fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, brand: String, quantity: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    // Validation: both name and quantity must be non-empty
    val isValid = name.isNotBlank() && quantity.isNotBlank() && quantity.toIntOrNull() != null && quantity.toIntOrNull()!! > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Material")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                onClick = { onAdd(name, brand, quantity) },
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
 * Allows updating name, brand, quantity, category, unit, price, and minimum stock
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
    var minStock by remember { mutableStateOf(material.minStock.toString()) }

    // Validation: name and quantity must be valid
    val isValid = name.isNotBlank() && quantity.toIntOrNull() != null && quantity.toIntOrNull()!! > 0

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

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Minimum Stock") },
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
                        price = price.toDoubleOrNull() ?: material.price,
                        minStock = minStock.toIntOrNull() ?: material.minStock
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
