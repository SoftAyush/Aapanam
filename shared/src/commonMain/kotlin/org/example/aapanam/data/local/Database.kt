package org.example.aapanam.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.aapanam.data.model.CreditPayment
import org.example.aapanam.data.model.Customer
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.PaymentMode
import org.example.aapanam.data.model.Sale
import org.example.aapanam.data.model.SaleItem
import org.example.aapanam.data.model.SalesSummary
import org.example.aapanam.db.AppDatabase
import kotlin.time.Instant
import org.example.aapanam.db.Customer as DbCustomer
import org.example.aapanam.db.Item as DbItem
import org.example.aapanam.db.Sale as DbSale

class Database(
    database: AppDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val itemQueries = database.itemQueries
    private val saleQueries = database.saleQueries
    private val creditPaymentQueries = database.creditPaymentQueries
    private val customerQueries = database.customerQueries

    // --- Item Functions ---
    fun getAllItems(): Flow<List<Item>> = itemQueries.getAllItems().asFlow().mapToList(dispatcher)
        .map { it.map { dbItem -> dbItem.toModel() } }

    fun getItemById(id: Long): Item? = itemQueries.getItemById(id).executeAsOneOrNull()?.toModel()
    fun searchItemsByName(name: String): Flow<List<Item>> =
        itemQueries.searchItemsByName(name).asFlow().mapToList(dispatcher)
            .map { it.map { dbItem -> dbItem.toModel() } }

    fun getLowStockItems(threshold: Long): Flow<List<Item>> =
        itemQueries.getLowStockItems(threshold).asFlow().mapToList(dispatcher)
            .map { it.map { dbItem -> dbItem.toModel() } }

    fun getUnsyncedItems(): List<Item> =
        itemQueries.getUnsyncedItems().executeAsList().map { it.toModel() }

    fun insertItem(item: Item) {
        itemQueries.insertItem(
            item.name,
            item.category,
            item.stockQuantity.toLong(),
            item.costPrice,
            item.sellingPrice,
            item.imageUrl,
            item.createdAt,
            item.lastModified.toEpochMilliseconds(),
            if (item.isSynced) 1L else 0L
        )
    }

    fun updateItem(item: Item) {
        itemQueries.updateItem(
            item.name,
            item.category,
            item.stockQuantity.toLong(),
            item.costPrice,
            item.sellingPrice,
            item.imageUrl,
            item.lastModified.toEpochMilliseconds(),
            if (item.isSynced) 1L else 0L,
            item.id
        )
    }

    fun upsertItem(item: Item) {
        itemQueries.upsertItem(
            id = item.id,
            name = item.name,
            category = item.category,
            quantity = item.stockQuantity.toLong(),
            cost_price = item.costPrice,
            selling_price = item.sellingPrice,
            image_url = item.imageUrl,
            created_at = item.createdAt,
            last_modified = item.lastModified.toEpochMilliseconds(),
            is_synced = if (item.isSynced) 1L else 0L
        )
    }

    fun updateStockQuantity(id: Long, quantity: Long, lastModified: Long) {
        itemQueries.updateStockQuantity(quantity, lastModified, id)
    }

    fun deleteItemById(id: Long) {
        itemQueries.deleteItemById(id)
    }

    fun markAllItemsAsSynced(lastModified: Long) {
        itemQueries.markAllItemsAsSynced(lastModified)
    }

    // --- Customer Functions ---
    fun getAllCustomers(): Flow<List<Customer>> =
        customerQueries.getAllCustomers().asFlow().mapToList(dispatcher)
            .map { it.map { dbCustomer -> dbCustomer.toModel() } }

    fun getCustomerById(id: Long): Customer? =
        customerQueries.getCustomerById(id).executeAsOneOrNull()?.toModel()

    fun insertCustomer(customer: Customer): Long {
        customerQueries.insertCustomer(
            name = customer.name,
            phone = customer.phone,
            address = customer.address,
            created_at = customer.createdAt.toEpochMilliseconds()
        )
        return customerQueries.last_insert_rowid().executeAsOne()
    }

    fun getUnsyncedCustomers(): List<Customer> = customerQueries.getUnsyncedCustomers().executeAsList().map { it.toModel() }

    fun updateCustomerSyncStatus(isSynced: Boolean, id: Long) {
        customerQueries.updateCustomerSyncStatus(if (isSynced) 1L else 0L, id)
    }

    // --- Sale Functions ---
    fun getSaleById(id: Long): Sale? = saleQueries.getSaleById(id).executeAsOneOrNull()
        ?.toModel(saleQueries.getSaleItemsBySaleId(id).executeAsList().map { it.toModel() })

    fun getUnsyncedSales(): List<Sale> = saleQueries.getUnsyncedSales().executeAsList()
        .map { dbSale ->
            dbSale.toModel(
                saleQueries.getSaleItemsBySaleId(dbSale.id).executeAsList().map { it.toModel() })
        }

    fun getAllSales(): Flow<List<Sale>> = saleQueries.getAllSales().asFlow().mapToList(dispatcher)
        .map { sales ->
            sales.map { dbSale ->
                dbSale.toModel(
                    saleQueries.getSaleItemsBySaleId(dbSale.id).executeAsList()
                        .map { it.toModel() })
            }
        }

    fun getTodaySales(): Flow<List<Sale>> =
        saleQueries.getTodaySales().asFlow().mapToList(dispatcher).map { sales ->
            sales.map { dbSale ->
                dbSale.toModel(
                    saleQueries.getSaleItemsBySaleId(dbSale.id).executeAsList()
                        .map { it.toModel() })
            }
        }

    fun getCreditSales(): Flow<List<Sale>> =
        saleQueries.getCreditSales().asFlow().mapToList(dispatcher).map { sales ->
            sales.map { dbSale ->
                dbSale.toModel(
                    saleQueries.getSaleItemsBySaleId(dbSale.id).executeAsList()
                        .map { it.toModel() })
            }
        }

    fun getSalesSummary(): Flow<SalesSummary> =
        saleQueries.getSalesSummary().asFlow().mapToOne(dispatcher).map { it.toModel() }

    fun getLatestSaleByCustomerId(id: Long): Flow<Sale?> =
        saleQueries.getSalesByCustomerId(id).asFlow().mapToOne(dispatcher).map { dbSale ->
            dbSale?.toModel(
                saleQueries.getSaleItemsBySaleId(dbSale.id).executeAsList()
                    .map { it.toModel() }
            )
        }

    fun insertSaleWithItems(sale: Sale): Sale {
        return saleQueries.transactionWithResult {
            saleQueries.insertSale(
                customer_id = sale.customerId,
                total_amount = sale.totalAmount,
                paid_amount = sale.paidAmount,
                payment_mode = sale.paymentMode.name,
                sale_date = sale.saleDate.toEpochMilliseconds(),
                is_credit = if (sale.isCredit) 1L else 0L,
                is_synced = if (sale.isSynced) 1L else 0L,
                created_at = sale.createdAt.toEpochMilliseconds()
            )
            val newSaleId = saleQueries.last_insert_rowid().executeAsOne()
            sale.items.forEach { saleItem ->
                saleQueries.insertSaleItem(
                    sale_id = newSaleId,
                    item_id = saleItem.itemId,
                    item_name = saleItem.itemName,
                    quantity = saleItem.quantity.toLong(),
                    unit_price = saleItem.unitPrice,
                    total_price = saleItem.totalPrice,
                    created_at = saleItem.createdAt.toEpochMilliseconds()
                )
            }
            sale.copy(id = newSaleId)
        }
    }

    fun upsertSaleWithItems(sale: Sale) {
        saleQueries.transaction {
            saleQueries.upsertSale(
                id = sale.id,
                customer_id = sale.customerId,
                total_amount = sale.totalAmount,
                paid_amount = sale.paidAmount,
                payment_mode = sale.paymentMode.name,
                sale_date = sale.saleDate.toEpochMilliseconds(),
                is_credit = if (sale.isCredit) 1L else 0L,
                is_synced = if (sale.isSynced) 1L else 0L,
                created_at = sale.createdAt.toEpochMilliseconds()
            )
            saleQueries.deleteSaleItemsBySaleId(sale.id)
            sale.items.forEach { saleItem ->
                saleQueries.insertSaleItem(
                    sale_id = sale.id,
                    item_id = saleItem.itemId,
                    item_name = saleItem.itemName,
                    quantity = saleItem.quantity.toLong(),
                    unit_price = saleItem.unitPrice,
                    total_price = saleItem.totalPrice,
                    created_at = saleItem.createdAt.toEpochMilliseconds()
                )
            }
        }
    }

    fun updateSalePayment(saleId: Long, paidAmount: Double, isCredit: Boolean) {
        saleQueries.updateSalePayment(paidAmount, if (isCredit) 1L else 0L, saleId)
    }

    fun insertCreditPayment(payment: CreditPayment) {
        creditPaymentQueries.insertCreditPayment(
            customer_id = payment.customerId,
            paid_amount = payment.paidAmount,
            payment_date = payment.paymentDate.toEpochMilliseconds(),
            notes = payment.notes,
            created_at = payment.createdAt.toEpochMilliseconds()
        )
    }

    fun getUnsyncedCreditPayments(): List<CreditPayment> = creditPaymentQueries.getUnsyncedCreditPayments().executeAsList().map { it.toModel() }

    fun updateCreditPaymentSyncStatus(isSynced: Boolean, id: Long) {
        creditPaymentQueries.updateCreditPaymentSyncStatus(if (isSynced) 1L else 0L, id)
    }

    fun deleteSaleById(id: Long) {
        saleQueries.deleteSaleById(id)
    }

    fun updateSaleSyncStatus(isSynced: Boolean, id: Long) {
        saleQueries.updateSaleSyncStatus(if (isSynced) 1L else 0L, id)
    }

    fun clearAllData() {
        saleQueries.deleteAllSales()
        saleQueries.deleteAllSaleItems()
        itemQueries.deleteAllItems()
        customerQueries.deleteAllCustomers()
    }
}

// --- Mapper Functions ---
fun DbItem.toModel(): Item = Item(
    id,
    name,
    category,
    quantity.toInt(),
    cost_price,
    selling_price,
    image_url,
    is_synced != 0L,
    created_at,
    Instant.fromEpochMilliseconds(last_modified)
)

fun org.example.aapanam.db.Sale_item.toModel(): SaleItem = SaleItem(
    id,
    sale_id,
    item_id,
    item_name,
    quantity.toInt(),
    unit_price,
    total_price,
    Instant.fromEpochMilliseconds(created_at)
)

fun DbSale.toModel(items: List<SaleItem>): Sale = Sale(
    id = id,
    customerId = customer_id,
    totalAmount = total_amount,
    paidAmount = paid_amount,
    paymentMode = PaymentMode.valueOf(payment_mode),
    saleDate = Instant.fromEpochMilliseconds(sale_date),
    isCredit = is_credit != 0L,
    isSynced = is_synced != 0L,
    createdAt = Instant.fromEpochMilliseconds(created_at),
    items = items
)

fun org.example.aapanam.db.GetSalesSummary.toModel(): SalesSummary = SalesSummary(
    total_sales,
    total_revenue ?: 0.0,
    average_sale ?: 0.0,
    total_paid ?: 0.0,
    total_credit ?: 0.0
)

fun org.example.aapanam.db.Credit_payment.toModel(): CreditPayment = CreditPayment(
    id,
    customer_id,
    paid_amount,
    Instant.fromEpochMilliseconds(payment_date),
    notes,
    Instant.fromEpochMilliseconds(created_at),
    is_synced != 0L
)

fun DbCustomer.toModel(): Customer = Customer(
    id,
    name,
    phone,
    address,
    Instant.fromEpochMilliseconds(created_at),
    is_synced != 0L
)
