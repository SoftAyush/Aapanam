package org.example.aapanam.presentation


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.aapanam.data.model.Customer
import org.example.aapanam.data.model.Sale
import org.example.aapanam.repository.Repository

class SalesViewModel(private val repository: Repository) : ViewModel() {

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales = _sales.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers = _customers.asStateFlow()

    init {
        getSales()
        getCustomers()
    }

    private fun getSales() {
        coroutineScope.launch {
            repository.getAllSales().collect { sales ->
                _sales.update { sales }
            }
        }
    }

    private fun getCustomers() {
        coroutineScope.launch {
            repository.getAllCustomers().collect { customerList ->
                _customers.update { customerList }
            }
        }
    }

    fun addSaleAndUpdateStock(sale: Sale, customer: Customer?) {
        coroutineScope.launch {
            val customerId: Long? = if (customer != null) {
                if (customer.id == 0L) {
                    // New customer, insert and get the new ID
                    repository.insertCustomer(customer)
                } else {
                    // Existing customer, use their ID
                    customer.id
                }
            } else {
                // No customer provided
                null
            }
            val newSale = sale.copy(customerId = customerId)
            repository.insertSaleAndUpdateStock(newSale)
        }
    }

    fun getLatestSaleByCustomer(customer: Customer, onResult: (Sale?) -> Unit) {
        coroutineScope.launch {
            customer.id.let {
                repository.getLatestSaleByCustomerId(it).collect { sale ->
                    onResult(sale)
                }
            }
        }
    }
}
