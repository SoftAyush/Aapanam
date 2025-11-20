package org.example.aapanam.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.example.aapanam.data.local.Database
import org.example.aapanam.data.model.CreditPayment
import org.example.aapanam.data.model.Customer
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.Sale
import org.example.aapanam.data.model.SalesSummary
import org.example.aapanam.data.remote.FirebaseService
import org.example.aapanam.util.ConnectivityManager
import kotlin.time.Clock

class Repository(
    private val database: Database,
    private val firebaseService: FirebaseService,
    private val connectivityManager: ConnectivityManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val userId: String?
        get() = firebaseService.getCurrentUser()?.uid

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

    // Removed: fun getLatestSaleByCustomer(name: String): Flow<Sale?> = database.getLatestSaleByCustomer(name)

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

        database.insertCreditPayment(
            CreditPayment(
                id = 0,
                customerId = sale.customerId!!, // Changed from saleId = sale.id to customerId = sale.customerId
                paidAmount = amount,
                paymentDate = Clock.System.now(),
                notes = "Paid by user",
                createdAt = Clock.System.now()
            )
        )
    }

    // --------------------- Customer Functions ---------------------
    fun getAllCustomers(): Flow<List<Customer>> = database.getAllCustomers()
    suspend fun insertCustomer(customer: Customer): Long = database.insertCustomer(customer)
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
            println("Sync failed for item $itemId: ${e.message}")
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
            println("Sync failed for sale $saleId: ${e.message}")
        }
    }

    suspend fun syncAllUnsynced() {
        if (connectivityManager.isNetworkAvailable()) {
            userId?.let {
                database.getUnsyncedItems().forEach { syncItem(it.id) }
                database.getUnsyncedSales().forEach { syncSale(it.id) }
            }
        }
    }

    suspend fun pull() {
        if (!connectivityManager.isNetworkAvailable()) return

        userId?.let { user ->

            // Pull Items
            firebaseService.getItems(user).forEach { remoteItem ->
                val localItem = database.getItemById(remoteItem.id)
                if (localItem == null || remoteItem.lastModified > localItem.lastModified) {
                    database.upsertItem(remoteItem)
                }
            }

            // Pull Sales
            firebaseService.getSales(user).forEach { remoteSale ->
                val localSale = database.getSaleById(remoteSale.id)
                if (localSale == null || remoteSale.createdAt > localSale.createdAt) {
                    database.upsertSaleWithItems(remoteSale)
                }
            }
        }
    }
}