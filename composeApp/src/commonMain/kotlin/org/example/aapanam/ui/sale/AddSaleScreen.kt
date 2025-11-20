package org.example.aapanam.ui.Sale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.aapanam.data.model.Customer
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.PaymentMode
import org.example.aapanam.data.model.Sale
import org.example.aapanam.data.model.SaleItem
import org.example.aapanam.presentation.InventoryViewModel
import org.example.aapanam.presentation.SalesViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    inventoryViewModel: InventoryViewModel = koinViewModel<InventoryViewModel>(),
    salesViewModel: SalesViewModel = koinViewModel<SalesViewModel>(),
    onSaleSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    val inventory by inventoryViewModel.items.collectAsState()
    var cart by remember { mutableStateOf<Map<Long, SaleItem>>(emptyMap()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerName by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var amountPaid by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showCustomerPopup by remember { mutableStateOf(false) }
    val customers by salesViewModel.customers.collectAsState()

    val filteredInventory = remember(inventory, searchQuery) {
        if (searchQuery.isBlank()) {
            inventory
        } else {
            inventory.filter { item ->
                item.name.contains(searchQuery, ignoreCase = true) ||
                        item.category?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val onAddItemToCart: (Item) -> Unit = { item ->
        val existingCartItem = cart[item.id]
        val currentStock = item.stockQuantity
        val cartQty = existingCartItem?.quantity ?: 0

        if (currentStock > cartQty) {
            if (existingCartItem != null) {
                val updatedItem = existingCartItem.copy(
                    quantity = existingCartItem.quantity + 1,
                    totalPrice = existingCartItem.unitPrice * (existingCartItem.quantity + 1)
                )
                cart = cart + (item.id to updatedItem)
            } else {
                val newCartItem = SaleItem(
                    itemId = item.id,
                    itemName = item.name,
                    quantity = 1,
                    unitPrice = item.sellingPrice,
                    totalPrice = item.sellingPrice,
                    saleId = 0,
                    id = 0,
                    createdAt = Clock.System.now()
                )
                cart = cart + (item.id to newCartItem)
            }
        }
    }

    val onRemoveItemFromCart: (SaleItem) -> Unit = { saleItem ->
        val existingCartItem = cart[saleItem.itemId]
        if (existingCartItem != null) {
            if (existingCartItem.quantity > 1) {
                val updatedItem = existingCartItem.copy(
                    quantity = existingCartItem.quantity - 1,
                    totalPrice = existingCartItem.unitPrice * (existingCartItem.quantity - 1)
                )
                cart = cart + (existingCartItem.itemId to updatedItem)
            } else {
                cart = cart - existingCartItem.itemId
            }
        }
    }

    val onIncrementItemInCart: (SaleItem) -> Unit = { saleItem ->
        inventory.find { it.id == saleItem.itemId }?.let {
            onAddItemToCart(it)
        }
    }

    val totalAmount = cart.values.sumOf { it.totalPrice }
    val isCreditSale = selectedPaymentMode == PaymentMode.CREDIT

    // Customer selection handler
    val onCustomerSelect: (Customer) -> Unit = { customer ->
        selectedCustomer = customer
        customerName = customer.name
        customerAddress = customer.address ?: ""
        customerPhone = customer.phone ?: ""
        showCustomerPopup = false
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
                            elevation = 16.dp,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        ),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 12.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = onCancel,
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Text(
                                    "NEW TRANSACTION",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    "Create Sale Record",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Cart Badge
                            if (cart.isNotEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${cart.values.sumOf { it.quantity }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Tab Layout
                        TabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        "PRODUCTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )

                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        "CART (${cart.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )

                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Text(
                                        "CUSTOMER",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBottomBar(
                    currentTab = selectedTab,
                    cartItems = cart.values.toList(),
                    onBackClick = {
                        if (selectedTab > 0) selectedTab--
                    },
                    onNextClick = {
                        if (selectedTab < 2 && cart.isNotEmpty()) selectedTab++
                    },
                    onConfirmSale = {
                        val paid =
                            if (isCreditSale) amountPaid.toDoubleOrNull() ?: 0.0 else totalAmount

                        val customerToSave = if (selectedCustomer != null && selectedCustomer?.name == customerName) {
                            selectedCustomer?.copy(
                                name = customerName,
                                address = customerAddress,
                                phone = customerPhone
                            )
                        } else if (customerName.isNotBlank()) {
                            Customer(
                                name = customerName,
                                address = customerAddress,
                                phone = customerPhone
                            )
                        } else {
                            null
                        }

                        val newSale = Sale(
                            items = cart.values.toList(),
                            totalAmount = totalAmount,
                            paidAmount = paid,
                            customerId = customerToSave?.id,
                            isCredit = isCreditSale,
                            paymentMode = selectedPaymentMode
                        )
                        salesViewModel.addSaleAndUpdateStock(newSale, customerToSave)
                        onSaleSaved()
                    },
                    totalAmount = totalAmount
                )
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> ProductsTab(
                    inventory = filteredInventory,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onAddItemToCart = onAddItemToCart,
                    cart = cart,
                    modifier = Modifier.padding(paddingValues)
                )

                1 -> CartTab(
                    cartItems = cart.values.toList(),
                    onIncrementItem = onIncrementItemInCart,
                    onRemoveItem = onRemoveItemFromCart,
                    modifier = Modifier.padding(paddingValues)
                )

                2 -> CustomerTab(
                    customerName = customerName,
                    customerAddress = customerAddress,
                    customerPhone = customerPhone,
                    selectedPaymentMode = selectedPaymentMode,
                    amountPaid = amountPaid,
                    onCustomerNameChange = { customerName = it },
                    onCustomerAddressChange = { customerAddress = it },
                    onCustomerPhoneChange = { customerPhone = it },
                    onPaymentModeChange = { selectedPaymentMode = it },
                    onAmountPaidChange = { amountPaid = it },
                    cartItems = cart.values.toList(),
                    modifier = Modifier.padding(paddingValues),
                    customers = customers,
                    onCustomerSelect = onCustomerSelect,
                    showCustomerPopup = showCustomerPopup,
                    onShowCustomerPopup = { showCustomerPopup = true },
                    onDismissCustomerPopup = { showCustomerPopup = false }
                )
            }
        }

        // Customer Selection Popup
        AnimatedVisibility(
            visible = showCustomerPopup,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
            )
        ) {
            CustomerSelectionPopup(
                customers = customers,
                onCustomerSelect = onCustomerSelect,
                onDismiss = { showCustomerPopup = false }
            )
        }
    }
}

@Composable
private fun CustomerSelectionPopup(
    customers: List<Customer>,
    onCustomerSelect: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(500.dp)
                    .padding(24.dp)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Select Customer",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Choose from existing customers",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Customer List
                    if (customers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "No customers",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Customers Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Add a new customer by typing their name",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(customers, key = { it.id}) { customer ->
                                CustomerSelectionItem(
                                    customer = customer,
                                    onClick = { onCustomerSelect(customer) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add New Customer Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Add new",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add New Customer",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerSelectionItem(
    customer: Customer,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Customer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                customer.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NavigationBottomBar(
    currentTab: Int,
    cartItems: List<SaleItem>,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onConfirmSale: () -> Unit,
    totalAmount: Double,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when (currentTab) {
                0 -> {
                    Spacer(modifier = Modifier.weight(1f))
                    NextButton(
                        onClick = onNextClick,
                        enabled = cartItems.isNotEmpty(),
                        text = "Next - Select Items"
                    )
                }

                1 -> {
                    BackButton(onClick = onBackClick)
                    Spacer(modifier = Modifier.weight(1f))
                    NextButton(
                        onClick = onNextClick,
                        enabled = cartItems.isNotEmpty(),
                        text = "Next - Customer Info"
                    )
                }

                2 -> {
                    BackButton(onClick = onBackClick)
                    Spacer(modifier = Modifier.weight(1f))
                    ConfirmSaleButton(
                        onClick = onConfirmSale,
                        enabled = cartItems.isNotEmpty(),
                        totalAmount = totalAmount
                    )
                }
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Back", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NextButton(onClick: () -> Unit, enabled: Boolean, text: String) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        enabled = enabled
    ) {
        Text(text, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = "Next",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ConfirmSaleButton(onClick: () -> Unit, enabled: Boolean, totalAmount: Double) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        enabled = enabled
    ) {
        Icon(Icons.Default.Check, contentDescription = "Confirm", modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Confirm Sale", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "रु${String.format("%.2f", totalAmount)}",
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ProductsTab(
    inventory: List<Item>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddItemToCart: (Item) -> Unit,
    cart: Map<Long, SaleItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SearchBarSection(
            searchQuery = searchQuery,
            onSearchChange = onSearchChange,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (inventory.isEmpty()) {
            if (searchQuery.isNotEmpty()) {
                EmptySearchState(searchQuery)
            } else {
                EmptyState(
                    title = "No Products",
                    subtitle = "Add products to inventory first",
                    icon = Icons.Default.Inventory2
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(inventory, key = { item -> item.id }) { item ->
                    val selectedQuantity = cart[item.id]?.quantity ?: 0
                    ProductCard(
                        item = item,
                        onAddItem = onAddItemToCart,
                        selectedQuantity = selectedQuantity
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search products by name or category...") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CartTab(
    cartItems: List<SaleItem>,
    onIncrementItem: (SaleItem) -> Unit,
    onRemoveItem: (SaleItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalAmount = cartItems.sumOf { it.totalPrice }

    Column(modifier = modifier) {
        if (cartItems.isEmpty()) {
            EmptyState(
                title = "Cart Empty",
                subtitle = "Add products from the Products tab",
                icon = Icons.Default.ShoppingCart
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Cart Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "${cartItems.size} items • ${cartItems.sumOf { it.quantity }} units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                "रु${String.format("%.2f", totalAmount)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(cartItems, key = { it.itemId }) { saleItem ->
                    CartItemCard(
                        saleItem = saleItem,
                        onIncrement = { onIncrementItem(saleItem) },
                        onDecrement = { onRemoveItem(saleItem) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerTab(
    customerName: String,
    customerAddress: String,
    customerPhone: String,
    selectedPaymentMode: PaymentMode,
    amountPaid: String,
    onCustomerNameChange: (String) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onAmountPaidChange: (String) -> Unit,
    cartItems: List<SaleItem>,
    customers: List<Customer>,
    onCustomerSelect: (Customer) -> Unit,
    showCustomerPopup: Boolean,
    onShowCustomerPopup: () -> Unit,
    onDismissCustomerPopup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalAmount = cartItems.sumOf { it.totalPrice }
    val isCreditSale = selectedPaymentMode == PaymentMode.CREDIT
    val isExistingCustomer = customers.any { it.name == customerName }

    Column(modifier = modifier) {
        if (cartItems.isEmpty()) {
            EmptyState(
                title = "No Items in Cart",
                subtitle = "Add products to cart first",
                icon = Icons.Default.ShoppingCart
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Customer Information Section
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Section Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Customer",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "CUSTOMER INFORMATION",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Customer Name Field with Select Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomerTextField(
                                    value = customerName,
                                    onValueChange = onCustomerNameChange,
                                    label = "Customer Name",
                                    icon = Icons.Default.Person,
                                    placeholder = "Enter customer name",
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = onShowCustomerPopup,
                                    modifier = Modifier.height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "Select customer",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Select")
                                }
                            }

                            // Customer Status
                            if (customerName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isExistingCustomer) Color(0xFFE8F5E8) else Color(0xFFE3F2FD),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isExistingCustomer) Color(0xFF4CAF50) else Color(0xFF2196F3)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (isExistingCustomer) Icons.Default.Check else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isExistingCustomer) Color(0xFF4CAF50) else Color(0xFF2196F3),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                if (isExistingCustomer) "Existing Customer" else "New Customer",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isExistingCustomer) Color(0xFF4CAF50) else Color(0xFF2196F3)
                                            )
                                            Text(
                                                if (isExistingCustomer) "Previous details loaded automatically"
                                                else "Creating new customer record",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isExistingCustomer) Color(0xFF4CAF50) else Color(0xFF2196F3)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            CustomerTextField(
                                value = customerAddress,
                                onValueChange = onCustomerAddressChange,
                                label = "Address",
                                icon = Icons.Default.LocationOn,
                                placeholder = "Enter customer address",
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CustomerTextField(
                                value = customerPhone,
                                onValueChange = onCustomerPhoneChange,
                                label = "Phone Number",
                                icon = Icons.Default.Phone,
                                placeholder = "Enter phone number",
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }
                }

                item {
                    // Payment Method Section
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Payment,
                                        contentDescription = "Payment",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "PAYMENT METHOD",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            PaymentMethodOption(
                                paymentMode = PaymentMode.CASH,
                                selectedPaymentMode = selectedPaymentMode,
                                onPaymentModeChange = onPaymentModeChange,
                                icon = Icons.Default.Money,
                                title = "Cash Payment",
                                description = "Pay with cash"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PaymentMethodOption(
                                paymentMode = PaymentMode.CARD,
                                selectedPaymentMode = selectedPaymentMode,
                                onPaymentModeChange = onPaymentModeChange,
                                icon = Icons.Default.CreditCard,
                                title = "Card Payment",
                                description = "Pay with debit/credit card"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PaymentMethodOption(
                                paymentMode = PaymentMode.DIGITAL,
                                selectedPaymentMode = selectedPaymentMode,
                                onPaymentModeChange = onPaymentModeChange,
                                icon = Icons.Default.QrCode,
                                title = "Online Payment",
                                description = "Pay with Mobile Banking/Digital wallets"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PaymentMethodOption(
                                paymentMode = PaymentMode.CREDIT,
                                selectedPaymentMode = selectedPaymentMode,
                                onPaymentModeChange = onPaymentModeChange,
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                title = "Credit Sale",
                                description = "Customer will pay later"
                            )

                            if (isCreditSale) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CustomerTextField(
                                    value = amountPaid,
                                    onValueChange = onAmountPaidChange,
                                    label = "Advance Amount Paid",
                                    icon = Icons.Default.AttachMoney,
                                    placeholder = "Enter advance amount paid",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }
                    }
                }

                item {
                    // Order Summary
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "ORDER SUMMARY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            cartItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${item.quantity}x ${item.itemName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                    Text(
                                        "रु${String.format("%.2f", item.totalPrice)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total Amount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "रु${String.format("%.2f", totalAmount)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    item: Item,
    onAddItem: (Item) -> Unit,
    selectedQuantity: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onAddItem(item) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

                // Stock Indicator with selected quantity
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            item.stockQuantity > 10 -> Color(0xFFE8F5E8)
                            item.stockQuantity > 0 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            "${item.stockQuantity} in stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                item.stockQuantity > 10 -> Color(0xFF4CAF50)
                                item.stockQuantity > 0 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Show selected quantity if any
                    if (selectedQuantity > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                "$selectedQuantity in cart",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "रु ${String.format("%.2f", item.sellingPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Selling Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart Button - Show different state if item is in cart
                if (selectedQuantity > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$selectedQuantity",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = { onAddItem(item) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to cart",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    saleItem: SaleItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    saleItem.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "रु${String.format("%.2f", saleItem.unitPrice)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = onDecrement,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    "${saleItem.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )

                Surface(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                "रु${String.format("%.2f", saleItem.totalPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PaymentMethodOption(
    paymentMode: PaymentMode,
    selectedPaymentMode: PaymentMode,
    onPaymentModeChange: (PaymentMode) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    val isSelected = selectedPaymentMode == paymentMode

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPaymentModeChange(paymentMode) },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        maxLines = maxLines
    )
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
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
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptySearchState(searchQuery: String) {
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
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No Products Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "No results for \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Try searching with different keywords",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
