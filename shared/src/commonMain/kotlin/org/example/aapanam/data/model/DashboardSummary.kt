package org.example.aapanam.data.model

/**
 * Dashboard summary data
 */
data class DashboardSummary(
    val totalItems: Int = 0,
    val lowStockItems: Int = 0,
    val todaySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalCredit: Double = 0.0
)