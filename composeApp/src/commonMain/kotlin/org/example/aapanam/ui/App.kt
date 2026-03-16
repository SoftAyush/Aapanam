package org.example.aapanam.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.Sale
import org.example.aapanam.presentation.AppTheme
import org.example.aapanam.presentation.AuthState
import org.example.aapanam.presentation.AuthViewModel
import org.example.aapanam.presentation.InventoryViewModel
import org.example.aapanam.presentation.SalesViewModel
import org.example.aapanam.presentation.ThemeViewModel
import org.example.aapanam.repository.Repository
import org.example.aapanam.ui.Sale.AddSaleScreen
import org.example.aapanam.ui.sale.SaleDetailScreen
import org.example.aapanam.ui.sale.SalesScreen
import org.example.aapanam.util.Logger
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    themeViewModel: ThemeViewModel = koinViewModel<ThemeViewModel>(),
    authViewModel: AuthViewModel = koinViewModel<AuthViewModel>(),
) {
    val theme by themeViewModel.theme.collectAsState()
    val useDarkTheme = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) darkScheme else lightScheme
    ) {
        val authState by authViewModel.authState.collectAsState()

        when (authState) {
            is AuthState.Authenticated -> {
                MainAppContent(authViewModel)
            }

            AuthState.Unauthenticated, is AuthState.Error -> {
                LoginScreen(authViewModel)
            }

            AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val inventoryViewModel: InventoryViewModel = koinViewModel<InventoryViewModel>()
    val salesViewModel: SalesViewModel = koinViewModel<SalesViewModel>()
    val repository: Repository = koinInject()

    LaunchedEffect(Unit) {
        while (true) {
            try {
                repository.syncAllUnsynced()
                repository.pull()
            } catch (e: Exception) {
                Logger.e("Background sync failed: ${e.message}", e)
            }
            delay(5 * 60 * 1000) // 5 minutes
        }
    }

    // Main router
    when (val screen = currentScreen) {
        is Screen.AddEditItem -> {
            AddEditItemScreen(
                itemToEdit = screen.item,
                onSave = {
                    if (screen.item != null) {
                        inventoryViewModel.updateItem(it)
                    } else {
                        inventoryViewModel.addItem(it)
                    }
                    currentScreen = Screen.Inventory
                },
                onCancel = { currentScreen = Screen.Inventory }
            )
        }

        is Screen.AddSale -> {
            AddSaleScreen(
                inventoryViewModel = inventoryViewModel,
                salesViewModel = salesViewModel,
                onSaleSaved = { currentScreen = Screen.Sales },
                onCancel = { currentScreen = Screen.Sales }
            )
        }

        is Screen.SaleDetail -> {
            SaleDetailScreen(
                sale = screen.sale,
                onBack = { currentScreen = Screen.Sales }
            )
        }

        is Screen.ViewReports -> {
            ViewReportsScreen(onBack = { currentScreen = Screen.Dashboard })
        }

        else -> {
            Scaffold(

                bottomBar = {
                    AppBottomNavigation(
                        currentScreen = currentScreen,
                        onScreenSelected = { currentScreen = it }
                    )
                }
            ) {
                Column(modifier = Modifier.padding(it)) {
                    when (currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(
                            authViewModel = authViewModel,
                            onCreditClick = { currentScreen = Screen.CreditCustomers },
                            onAddProductClick = { currentScreen = Screen.AddEditItem(null) },
                            onSalesClick = { currentScreen = Screen.Sales },
                            onManageStockClick = { currentScreen = Screen.Inventory },
                            onAlertsClick = { currentScreen = Screen.LowStock },
                            onLowStockClick = { currentScreen = Screen.LowStock },
                            onViewReportsClick = { currentScreen = Screen.ViewReports }
                        )

                        is Screen.Inventory -> InventoryScreen(
                            viewModel = inventoryViewModel,
                            onAddItem = { currentScreen = Screen.AddEditItem(null) },
                            onEditItem = { item -> currentScreen = Screen.AddEditItem(item) }
                        )

                        is Screen.Sales -> SalesScreen(
                            viewModel = salesViewModel,
                            onAddSale = { currentScreen = Screen.AddSale },
                            onSaleClick = { sale -> currentScreen = Screen.SaleDetail(sale) }
                        )

                        is Screen.Settings -> SettingsScreen(
                            koinViewModel(),
                            koinViewModel(),
                            authViewModel
                        )

                        is Screen.CreditCustomers -> CreditCustomersScreen()
                        is Screen.LowStock -> LowStockScreen(onBack = { currentScreen = Screen.Dashboard })
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
) {
    NavigationBar {
        val items = listOf(Screen.Dashboard, Screen.Inventory, Screen.Sales, Screen.Settings)
        items.forEach {
            NavigationBarItem(
                icon = { Icon(it.icon, contentDescription = it.title) },
                label = { Text(it.title) },
                selected = currentScreen == it,
                onClick = { onScreenSelected(it) }
            )
        }
    }
}

sealed class Screen(val title: String, val icon: ImageVector) {
    object Dashboard : Screen("Dashboard", Icons.Default.Dashboard)
    object Inventory : Screen("Inventory", Icons.AutoMirrored.Filled.List)
    object Sales : Screen("Sales", Icons.Default.ShoppingCart)
    object Settings : Screen("Settings", Icons.Default.Settings)
    object CreditCustomers : Screen("Credit Customers", Icons.Default.CreditCard)
    object LowStock : Screen("Low Stock", Icons.Default.Warning)
    object ViewReports : Screen("View Reports", Icons.Default.Assessment)
    data class AddEditItem(val item: Item?) :
        Screen("Add/Edit Item", Icons.AutoMirrored.Filled.List)

    object AddSale : Screen("Add Sale", Icons.Default.ShoppingCart)
    data class SaleDetail(val sale: Sale) : Screen("Sale Detail", Icons.Default.Receipt)
}
