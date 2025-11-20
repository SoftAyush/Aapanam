package org.example.aapanam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Sale(
    val id: Long = 0,
    val customerId: Long?,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val saleDate: Instant = Clock.System.now(),
    val isCredit: Boolean = false,
    @SerialName("isSynced")
    val isSynced: Boolean = false,
    val createdAt:Instant = Clock.System.now(),
    val items: List<SaleItem> = emptyList(),
    val customer: Customer? = null
)

@Serializable
data class SaleItem(
    val id: Long = 0,
    val saleId: Long,
    val itemId: Long,
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val createdAt:Instant = Clock.System.now()
)
enum class PaymentMode {
    CASH, CREDIT, DIGITAL,CARD
}
