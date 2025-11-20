package org.example.aapanam.presentation

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.example.aapanam.data.model.DashboardSummary
import org.example.aapanam.repository.Repository
import kotlin.time.Clock
import org.example.aapanam.data.model.Item

class DashboardViewModel(repository: Repository) : ViewModel() {

    val summary: StateFlow<DashboardSummary> = combine(
        repository.getAllItems(),
        repository.getAllSales(),
        repository.getSalesSummary()
    ) { items, sales, salesSummary ->
        val totalItems = items.sumOf { item: Item -> item.stockQuantity.toDouble() }.toInt()
        val lowStockItems = items.count { it.stockQuantity <= 10 } // Assuming low stock is <= 5
        val todaySales =
            sales.filter { it.saleDate.epochSeconds > (Clock.System.now().epochSeconds - 86400) }
                .sumOf { it.totalAmount }
        val monthlySales =
            sales.filter { it.saleDate.epochSeconds > (Clock.System.now().epochSeconds - (86400 * 30)) }
                .sumOf { it.totalAmount }
        // Profit calculation is a simplification
        val totalProfit = sales.sumOf { sale ->
            sale.items.sumOf { saleItem ->
                val item = items.find { it.id == saleItem.itemId }
                val cost = item?.costPrice ?: 0.0
                (saleItem.unitPrice - cost) * saleItem.quantity
            }
        }

        DashboardSummary(
            totalItems = totalItems,
            lowStockItems = lowStockItems,
            todaySales = todaySales,
            monthlySales = monthlySales,
            totalProfit = totalProfit,
            totalCredit = salesSummary.totalCredit
        )
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())
}