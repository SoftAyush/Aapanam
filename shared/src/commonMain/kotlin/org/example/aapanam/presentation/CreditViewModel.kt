package org.example.aapanam.presentation


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.aapanam.data.local.Database
import org.example.aapanam.data.model.Sale

class CreditViewModel(private val database: Database) : ViewModel() {

    private val _creditSales = MutableStateFlow<List<Sale>>(emptyList())
    val creditSales = _creditSales.asStateFlow()

    init {
        coroutineScope.launch {
            database.getCreditSales().collect { sales ->
                val salesWithCustomers = sales.map { sale ->
                    val customer = sale.customerId?.let { database.getCustomerById(it) }
                    sale.copy(customer = customer)
                }
                _creditSales.update { salesWithCustomers }
            }
        }
    }



    fun payDues(saleId: Long, amount: Double) {
        coroutineScope.launch {
            val sale = database.getSaleById(saleId)
            if (sale != null) {
                val newPaidAmount = sale.paidAmount + amount
                val isCredit = newPaidAmount < sale.totalAmount
                database.updateSalePayment(saleId, newPaidAmount, isCredit)
            }
        }
    }
}
