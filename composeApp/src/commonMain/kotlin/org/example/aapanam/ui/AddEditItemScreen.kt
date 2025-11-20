package org.example.aapanam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import org.example.aapanam.data.model.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemToEdit: Item? = null,
    onSave: (Item) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "") }
    var quantity by remember { mutableStateOf(itemToEdit?.stockQuantity?.toString() ?: "") }
    var costPrice by remember { mutableStateOf(itemToEdit?.costPrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(itemToEdit?.sellingPrice?.toString() ?: "") }

    // Validation states
    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var costPriceError by remember { mutableStateOf(false) }
    var sellingPriceError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    fun validateForm(): Boolean {
        nameError = name.isBlank()
        quantityError = quantity.isBlank() || quantity.toIntOrNull() == null
        costPriceError = costPrice.isBlank() || costPrice.toDoubleOrNull() == null
        sellingPriceError = sellingPrice.isBlank() || sellingPrice.toDoubleOrNull() == null

        return !nameError && !quantityError && !costPriceError && !sellingPriceError
    }

    fun saveItem() {
        if (validateForm()) {
            val newItem = (itemToEdit ?: Item(
                name = "",
                stockQuantity = 0,
                costPrice = 0.0,
                sellingPrice = 0.0,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )).copy(
                name = name.trim(),
                category = category.trim(),
                stockQuantity = quantity.toIntOrNull() ?: 0,
                costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                lastModified = Clock.System.now()
            )
            onSave(newItem)
        }
    }

    // Calculate profit/loss
    val cost = costPrice.toDoubleOrNull() ?: 0.0
    val selling = sellingPrice.toDoubleOrNull() ?: 0.0
    val profitLoss = selling - cost
    val profitPercentage = if (cost > 0) (profitLoss / cost) * 100 else 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button with premium design
                        Surface(
                            onClick = onCancel,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                if (itemToEdit == null) "ADD NEW ITEM" else "EDIT ITEM",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                if (itemToEdit == null) "Create inventory item" else "Update item details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Quick Stats if editing
                    itemToEdit?.let {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatChip(
                                icon = Icons.Default.Inventory,
                                value = it.stockQuantity.toString(),
                                label = "In Stock"
                            )
                            StatChip(
                                icon = Icons.Default.MonetizationOn,
                                value = "$${it.costPrice}",
                                label = "Cost"
                            )
                            StatChip(
                                icon = Icons.Default.PriceCheck,
                                value = "$${it.sellingPrice}",
                                label = "Price"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Form Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            clip = true
                        ),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Form Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "PRODUCT DETAILS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Form Fields
                        PremiumTextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            label = "Product Name",
                            icon = Icons.Default.ShoppingCart,
                            isError = nameError,
                            errorText = "Product name is required",
                            isRequired = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PremiumTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = "Category",
                            icon = Icons.Default.Category,
                            isRequired = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            PremiumTextField(
                                value = quantity,
                                onValueChange = {
                                    if (it.length <= 6) {
                                        quantity = it
                                        quantityError = false
                                    }
                                },
                                label = "Quantity",
                                icon = Icons.Default.Inventory,
                                isError = quantityError,
                                errorText = "Valid quantity required",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f),
                                isRequired = true
                            )

                            PremiumTextField(
                                value = costPrice,
                                onValueChange = {
                                    if (it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                        costPrice = it
                                        costPriceError = false
                                    }
                                },
                                label = "Cost Price",
                                icon = Icons.Default.MonetizationOn,
                                isError = costPriceError,
                                errorText = "Valid cost required",
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f),
                                isRequired = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PremiumTextField(
                            value = sellingPrice,
                            onValueChange = {
                                if (it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                    sellingPrice = it
                                    sellingPriceError = false
                                }
                            },
                            label = "Selling Price",
                            icon = Icons.Default.PriceCheck,
                            isError = sellingPriceError,
                            errorText = "Valid price required",
                            keyboardType = KeyboardType.Decimal,
                            isRequired = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Profit/Loss Card - Animated
                if (cost > 0 && selling > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        color = when {
                            profitLoss > 0 -> Color(0xFFE8F5E8)
                            profitLoss < 0 -> Color(0xFFFFEBEE)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        profitLoss > 0 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        profitLoss < 0 -> Color(0xFFF44336).copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        when {
                                            profitLoss > 0 -> Icons.Default.TrendingUp
                                            profitLoss < 0 -> Icons.Default.TrendingDown
                                            else -> Icons.Default.Remove
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            profitLoss > 0 -> Color(0xFF4CAF50)
                                            profitLoss < 0 -> Color(0xFFF44336)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "PROFIT ANALYSIS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        profitLoss > 0 -> Color(0xFF4CAF50)
                                        profitLoss < 0 -> Color(0xFFF44336)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        "Profit/Loss",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "रु ${String.format("%.2f", profitLoss)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            profitLoss > 0 -> Color(0xFF4CAF50)
                                            profitLoss < 0 -> Color(0xFFF44336)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Margin",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${String.format("%.1f", profitPercentage)}%",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            profitPercentage > 0 -> Color(0xFF4CAF50)
                                            profitPercentage < 0 -> Color(0xFFF44336)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CANCEL")
                    }

                    // Save Button
                    Button(
                        onClick = { saveItem() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE ITEM")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    errorText: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label)
                    if (isRequired) {
                        Text(
                            " *",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(errorText, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            )

        )
    }
}

@Composable
fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}