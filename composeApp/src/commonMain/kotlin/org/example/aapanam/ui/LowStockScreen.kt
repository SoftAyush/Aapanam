package org.example.aapanam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.aapanam.data.model.Item
import org.example.aapanam.presentation.LowStockViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowStockScreen(
    viewModel: LowStockViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val lowStockItems by viewModel.lowStockItems.collectAsState()

    val criticalItems = remember(lowStockItems) {
        lowStockItems.filter { it.stockQuantity == 0 }
    }
    val warningItems = remember(lowStockItems) {
        lowStockItems.filter { it.stockQuantity in 1..5 }
    }
    val lowItems = remember(lowStockItems) {
        lowStockItems.filter { it.stockQuantity in 6..10 }
    }

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
        Scaffold(
            topBar = {
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = onBack,
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
                                    "STOCK ALERTS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "Low Stock Items",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StockStatChip(
                                value = criticalItems.size.toString(),
                                label = "Out of Stock",
                                color = Color(0xFFF44336),
                                icon = Icons.Default.Dangerous
                            )
                            StockStatChip(
                                value = warningItems.size.toString(),
                                label = "Critical",
                                color = Color(0xFFFF9800),
                                icon = Icons.Default.Warning
                            )
                            StockStatChip(
                                value = lowItems.size.toString(),
                                label = "Low Stock",
                                color = Color(0xFFFFC107),
                                icon = Icons.Default.Info
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        ) { paddingValues ->
            if (lowStockItems.isEmpty()) {
                EmptyStockState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Critical Items (Out of Stock)
                    if (criticalItems.isNotEmpty()) {
                        item {
                            StockSectionHeader(
                                title = "OUT OF STOCK",
                                subtitle = "Immediate attention required",
                                color = Color(0xFFF44336),
                                icon = Icons.Default.Dangerous
                            )
                        }
                        items(criticalItems, key = { it.id }) { item ->
                            PremiumLowStockItem(item = item, severity = StockSeverity.CRITICAL)
                        }
                    }

                    // Warning Items (Critical Stock)
                    if (warningItems.isNotEmpty()) {
                        item {
                            StockSectionHeader(
                                title = "CRITICAL STOCK",
                                subtitle = "Very low inventory",
                                color = Color(0xFFFF9800),
                                icon = Icons.Default.Warning
                            )
                        }
                        items(warningItems, key = { it.id }) { item ->
                            PremiumLowStockItem(item = item, severity = StockSeverity.WARNING)
                        }
                    }

                    // Low Items
                    if (lowItems.isNotEmpty()) {
                        item {
                            StockSectionHeader(
                                title = "LOW STOCK",
                                subtitle = "Consider restocking soon",
                                color = Color(0xFFFFC107),
                                icon = Icons.Default.Info
                            )
                        }
                        items(lowItems, key = { it.id }) { item ->
                            PremiumLowStockItem(item = item, severity = StockSeverity.LOW)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockSectionHeader(title: String, subtitle: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumLowStockItem(item: Item, severity: StockSeverity) {
    val (backgroundColor, textColor, iconColor, statusText) = when (severity) {
        StockSeverity.CRITICAL -> StockStyle(
            Color(0xFFFFEBEE),
            Color(0xFFF44336),
            Color(0xFFF44336),
            "Out of Stock"
        )
        StockSeverity.WARNING -> StockStyle(
            Color(0xFFFFF3E0),
            Color(0xFFFF9800),
            Color(0xFFFF9800),
            "Critical"
        )
        StockSeverity.LOW -> StockStyle(
            Color(0xFFFFFDE7),
            Color(0xFFFFC107),
            Color(0xFFFFC107),
            "Low Stock"
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    modifier = Modifier.size(60.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (severity) {
                            StockSeverity.CRITICAL -> Icon(
                                Icons.Default.Dangerous,
                                contentDescription = "Critical",
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                            StockSeverity.WARNING -> Icon(
                                Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                            StockSeverity.LOW -> Icon(
                                Icons.Default.Info,
                                contentDescription = "Low",
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Product Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                item.category?.takeIf { it.isNotEmpty() }?.let { category ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock Information
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Stock
                    Column {
                        Text(
                            "Current Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${item.stockQuantity} units",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    // Selling Price
                    Column {
                        Text(
                            "Selling Price",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "रु${String.format("%.2f", item.sellingPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Urgency Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = textColor,
                    modifier = Modifier.size(12.dp)
                ) {
                    // Empty content for the dot
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when (severity) {
                        StockSeverity.CRITICAL -> "HIGH"
                        StockSeverity.WARNING -> "MEDIUM"
                        StockSeverity.LOW -> "LOW"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                Text(
                    "URGENCY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StockStatChip(value: String, label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStockState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(32.dp)
                    .size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "All Items Well Stocked",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Great job! All your inventory items have sufficient stock levels.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE8F5E8),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                "✅ Inventory management is optimal",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

enum class StockSeverity {
    CRITICAL, // 0 stock
    WARNING,  // 1-5 stock
    LOW       // 6-10 stock
}
data class StockStyle(
    val background: Color,
    val text: Color,
    val icon: Color,
    val label: String
)