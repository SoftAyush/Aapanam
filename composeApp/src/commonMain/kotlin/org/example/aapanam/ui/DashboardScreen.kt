package org.example.aapanam.ui

import android.app.Activity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import org.example.aapanam.data.model.DashboardSummary
import org.example.aapanam.presentation.AuthState
import org.example.aapanam.presentation.AuthViewModel
import org.example.aapanam.presentation.DashboardViewModel
import org.koin.androidx.compose.koinViewModel
import kotlinx.datetime.*
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onLowStockClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onCreditClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onViewReportsClick: () -> Unit = {},
    onManageStockClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = koinViewModel()
    val summary by viewModel.summary.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val quickActions = listOf(
        QuickAction(
            "Add Product",
            Icons.Filled.ShoppingCart,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            onAddProductClick
        ),
        QuickAction(
            "View Reports",
            Icons.AutoMirrored.Filled.TrendingUp,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
            onViewReportsClick
        ),
        QuickAction(
            "Manage Stock",
            Icons.AutoMirrored.Filled.List,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer,
            onManageStockClick
        ),
        QuickAction(
            "Alerts",
            Icons.Filled.Warning,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
            onAlertsClick
        )
    )

    // Status bar configuration
    val view = LocalView.current
    val context = LocalContext.current
    val topBarColor = MaterialTheme.colorScheme.surface




    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            window.statusBarColor = topBarColor.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Dashboard,
                                contentDescription = "Dashboard",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                "BUSINESS DASHBOARD",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Overview & Analytics",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(paddingValues),
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    PremiumWelcomeSection(authState)
                }

                item {
                    PremiumSummaryGrid(
                        summary = summary,
                        onLowStockClick = onLowStockClick,
                        onSalesClick = onSalesClick,
                        onCreditClick = onCreditClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    PremiumSectionHeader(
                        title = "QUICK ACTIONS",
                        subtitle = "Frequently used features"
                    )
                }

                items(
                    items = quickActions,
                    key = { it.name }
                ) { action ->
                    PremiumQuickActionItem(
                        action = action,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PremiumSectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Section",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 44.dp)
            )
        }
    }
}

@Composable
private fun PremiumWelcomeSection(authState: AuthState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Good ${getTimeOfDayGreeting()} ! ${getTimeOfDayEmoji()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (authState) {
                    is AuthState.Authenticated -> {
                        Text(
                            text = "Welcome back, ${authState.email}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text(
                            text = "Welcome to Aapanam Inventory",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "📈 Business is looking great today!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getTimeOfDayEmoji(),
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumSummaryGrid(
    summary: DashboardSummary,
    onLowStockClick: () -> Unit,
    onSalesClick: () -> Unit,
    onCreditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summaryCards = listOf(
        PremiumSummaryCardData(
            title = "Today's Revenue",
            value = formatCurrency(summary.todaySales),
            subtitle = "Sales performance",
            icon = Icons.Filled.ShoppingCart,
            color = Color(0xFF4CAF50),
            onClick = onSalesClick
        ),
        PremiumSummaryCardData(
            title = "Total Profit",
            value = formatCurrency(summary.totalProfit),
            subtitle = "Net earnings",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = Color(0xFF2196F3),
            onClick = null
        ),
        PremiumSummaryCardData(
            title = "Low Stock Alerts",
            value = summary.lowStockItems.toString(),
            subtitle = "Need restocking",
            icon = Icons.Filled.Warning,
            color = Color(0xFFF44336),
            onClick = onLowStockClick
        ),
        PremiumSummaryCardData(
            title = "Credit Balance",
            value = formatCurrency(summary.totalCredit),
            subtitle = "Outstanding amount",
            icon = Icons.Filled.CreditCard,
            color = Color(0xFFFF9800),
            onClick = onCreditClick
        ),
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        summaryCards.chunked(2).forEachIndexed { index, rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { cardData ->
                    PremiumSummaryCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp),
                        data = cardData
                    )
                }
                // Add spacers if row has less than 3 items
                repeat(2 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class PremiumSummaryCardData(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: (() -> Unit)? = null
)

@Composable
private fun PremiumSummaryCard(
    data: PremiumSummaryCardData,
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (data.onClick != null) elevation else 4.dp,
        animationSpec = tween(durationMillis = 200),
        label = "card_elevation"
    )

    Surface(
        modifier = modifier
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                enabled = data.onClick != null,
                onClick = data.onClick ?: {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (data.onClick != null) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = data.color.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = data.title,
                        tint = data.color,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp)
                    )
                }

                if (data.onClick != null) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = data.color.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Content
            Column {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private data class QuickAction(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun PremiumQuickActionItem(action: QuickAction, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                onClick = action.onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Container
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = action.backgroundColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.name,
                        tint = action.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getActionDescription(action.name),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Navigation Arrow
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

// Helper functions
private fun getTimeOfDayGreeting(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return when (now.hour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..20 -> "evening"
        else -> "night"
    }
}

private fun getTimeOfDayEmoji(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return when (now.hour) {
        in 5..11 -> "🌞"
        in 12..16 -> "☀️"
        in 17..20 -> "🌇"
        else -> "🌙"
    }
}

private fun getActionDescription(actionName: String): String {
    return when (actionName) {
        "Add Product" -> "Add new items to your inventory"
        "View Reports" -> "Analyze sales performance and trends"
        "Manage Stock" -> "Update inventory levels and track items"
        "Alerts" -> "View important notifications and warnings"
        else -> "Quick access to essential features"
    }
}

private fun formatCurrency(amount: Double): String {
    val value = if (amount % 1 == 0.0) amount.toInt().toString() else String.format("%.2f", amount)
    return "रु $value"
}