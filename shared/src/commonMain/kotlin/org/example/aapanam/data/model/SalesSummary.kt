package org.example.aapanam.data.model

data class SalesSummary(
    val totalSales: Long,
    val totalRevenue: Double,
    val averageSale: Double,
    val totalPaid: Double,
    val totalCredit: Double
) {
    companion object {
        fun empty() = SalesSummary(
            totalSales = 0,
            totalRevenue = 0.0,
            averageSale = 0.0,
            totalPaid = 0.0,
            totalCredit = 0.0
        )
    }
}
