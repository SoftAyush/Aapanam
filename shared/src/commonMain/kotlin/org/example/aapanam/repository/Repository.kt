package org.example.aapanam.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.aapanam.data.local.Database
import org.example.aapanam.data.model.CreditPayment
import org.example.aapanam.data.model.Customer
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.Sale
import org.example.aapanam.data.model.SalesSummary
import org.example.aapanam.data.remote.FirebaseService
import org.example.aapanam.util.ConnectivityManager
import org.example.aapanam.util.Logger
import kotlin.time.Clock

class Repository(
    private val database: Database,
    private val firebaseService: FirebaseService,
    private val connectivityManager: ConnectivityManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val userId: String?
        get() = firebaseService.getCurrentUser()?.uid

    private val syncMutex = Mutex()

    // --------------------- Item Functions ---------------------
    fun getAllItems(): Flow<List<Item>> = database.getAllItems()
    fun searchItems(query: String): Flow<List<Item>> = database.searchItemsByName(query)
    fun getLowStockItems(threshold: Long): Flow<List<Item>> = database.getLowStockItems(threshold)

    fun insertItem(item: Item) {
        val newItem = item.copy(
            createdAt = Clock.System.now().toEpochMilliseconds(),
            lastModified = Clock.System.now()
        )
        database.insertItem(newItem)

        coroutineScope.launch {
            syncItem(newItem.id)
        }
    }

    fun updateItem(item: Item) {
        val updatedItem = item.copy(
            lastModified = Clock.System.now(),
            isSynced = false
        )
        database.updateItem(updatedItem)

        coroutineScope.launch {
            syncItem(updatedItem.id)
        }
    }

    fun deleteItem(itemId: Long) {
        database.deleteItemById(itemId)
        coroutineScope.launch {
            userId?.let { firebaseService.deleteItem(it, itemId.toString()) }
        }
    }

    // --------------------- Sales Functions ---------------------
    fun getAllSales(): Flow<List<Sale>> = database.getAllSales()
    fun getTodaySales(): Flow<List<Sale>> = database.getTodaySales()
    fun getSalesSummary(): Flow<SalesSummary> = database.getSalesSummary()
    fun getCreditSales(): Flow<List<Sale>> = database.getCreditSales()

    fun getAllCustomerNames(): Flow<List<String>> =
        getAllSales().map { sales ->
            sales.mapNotNull { it.customer?.name }.distinct()
        }

    fun insertSaleAndUpdateStock(sale: Sale) {
        val newSale = database.insertSaleWithItems(
            sale.copy(createdAt = Clock.System.now())
        )

        newSale.items.forEach { saleItem ->
            database.getItemById(saleItem.itemId)?.let { item ->
                val updatedStock = item.stockQuantity - saleItem.quantity
                database.updateStockQuantity(
                    saleItem.itemId,
                    updatedStock.toLong(),
                    Clock.System.now().toEpochMilliseconds()
                )
            }
        }

        coroutineScope.launch { syncSale(newSale.id) }
    }

    fun payCredit(sale: Sale, amount: Double) {
        val newPaidAmount = sale.paidAmount + amount
        val isCredit = newPaidAmount < sale.totalAmount

        database.updateSalePayment(sale.id, newPaidAmount, isCredit)

        val creditPayment = CreditPayment(
            id = 0,
            customerId = sale.customerId!!,
            paidAmount = amount,
            paymentDate = Clock.System.now(),
            notes = "Paid by user",
            createdAt = Clock.System.now()
        )
        database.insertCreditPayment(creditPayment)
        coroutineScope.launch {
            syncCreditPayment(creditPayment)
        }
    }

    // --------------------- Customer Functions ---------------------
    fun getAllCustomers(): Flow<List<Customer>> = database.getAllCustomers()

    suspend fun insertCustomer(customer: Customer): Long {
        val newCustomerId = database.insertCustomer(customer)
        coroutineScope.launch {
            syncCustomer(newCustomerId)
        }
        return newCustomerId
    }
    fun getLatestSaleByCustomerId(customerId: Long): Flow<Sale?> =
        database.getLatestSaleByCustomerId(customerId)

    // --------------------- Sync Logic ---------------------
    private suspend fun syncItem(itemId: Long) {
        if (!connectivityManager.isNetworkAvailable()) return

        try {
            userId?.let { user ->
                database.getItemById(itemId)?.let { item ->
                    firebaseService.addItem(user, item)
                    database.updateItem(item.copy(isSynced = true))
                }
            }
        } catch (e: Exception) {
            Logger.e("Sync failed for item $itemId: ${e.message}", e)
        }
    }

    private suspend fun syncSale(saleId: Long) {
        if (!connectivityManager.isNetworkAvailable()) return

        try {
            userId?.let { user ->
                database.getSaleById(saleId)?.let { sale ->
                    firebaseService.addSale(user, sale)
                    database.updateSaleSyncStatus(true, sale.id)

                    sale.items.forEach { saleItem ->
                        syncItem(saleItem.itemId)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("Sync failed for sale $saleId: ${e.message}", e)
        }
    }

    private suspend fun syncCustomer(customerId: Long) {
        if (!connectivityManager.isNetworkAvailable()) return

        try {
            userId?.let { user ->
                database.getCustomerById(customerId)?.let { customer ->
                    firebaseService.addCustomer(user, customer)
                    database.updateCustomerSyncStatus(true, customerId)
                }
            }
        } catch (e: Exception) {
            Logger.e("Sync failed for customer $customerId: ${e.message}", e)
        }
    }

    private suspend fun syncCreditPayment(payment: CreditPayment) {
        if (!connectivityManager.isNetworkAvailable()) return

        try {
            userId?.let { user ->
                firebaseService.addCreditPayment(user, payment)
                database.updateCreditPaymentSyncStatus(true, payment.id)
            }
        } catch (e: Exception) {
            Logger.e("Sync failed for credit payment ${payment.id}: ${e.message}", e)
        }
    }

    suspend fun syncAllUnsynced() {
        if (!connectivityManager.isNetworkAvailable()) return
        
        syncMutex.withLock {
            val user = userId ?: return@withLock
            Logger.d("Starting batch sync for all unsynced data")

            try {
                // Batch sync Items
                val unsyncedItems = database.getUnsyncedItems()
                if (unsyncedItems.isNotEmpty()) {
                    firebaseService.addItems(user, unsyncedItems)
                    unsyncedItems.forEach { database.updateItem(it.copy(isSynced = true)) }
                }

                // Batch sync Customers
                val unsyncedCustomers = database.getUnsyncedCustomers()
                if (unsyncedCustomers.isNotEmpty()) {
                    firebaseService.addCustomers(user, unsyncedCustomers)
                    unsyncedCustomers.forEach { database.updateCustomerSyncStatus(true, it.id) }
                }

                // Batch sync Sales
                val unsyncedSales = database.getUnsyncedSales()
                if (unsyncedSales.isNotEmpty()) {
                    firebaseService.addSales(user, unsyncedSales)
                    unsyncedSales.forEach { database.updateSaleSyncStatus(true, it.id) }
                }

                // Batch sync Credit Payments
                val unsyncedPayments = database.getUnsyncedCreditPayments()
                if (unsyncedPayments.isNotEmpty()) {
                    firebaseService.addCreditPayments(user, unsyncedPayments)
                    unsyncedPayments.forEach { database.updateCreditPaymentSyncStatus(true, it.id) }
                }
                
                Logger.d("Batch sync completed successfully")
            } catch (e: Exception) {
                Logger.e("Batch sync failed: ${e.message}", e)
            }
        }
    }

    suspend fun pull() {
        if (!connectivityManager.isNetworkAvailable()) return

        syncMutex.withLock {
            val user = userId ?: return@withLock
            Logger.d("Pulling data from Firestore")

            try {
                // Pull Items
                val remoteItems = firebaseService.getItems(user)
                remoteItems.forEach { remoteItem ->
                    val localItem = database.getItemById(remoteItem.id)
                    if (localItem == null || remoteItem.lastModified > localItem.lastModified) {
                        database.upsertItem(remoteItem.copy(isSynced = true))
                    }
                }

                // Pull Sales
                val remoteSales = firebaseService.getSales(user)
                remoteSales.forEach { remoteSale ->
                    val localSale = database.getSaleById(remoteSale.id)
                    if (localSale == null || remoteSale.createdAt > localSale.createdAt) {
                        database.upsertSaleWithItems(remoteSale.copy(isSynced = true))
                    }
                }
                
                Logger.d("Pull completed successfully")
            } catch (e: Exception) {
                Logger.e("Pull failed: ${e.message}", e)
            }
        }
    }
}