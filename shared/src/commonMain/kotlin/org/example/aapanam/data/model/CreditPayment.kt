package org.example.aapanam.data.model

import kotlin.time.Clock
import kotlin.time.Instant

data class CreditPayment(
    val id: Long,
    val customerId: Long, // Changed from saleId
    val paidAmount: Double,
    val paymentDate: Instant,
    val notes: String?,
    val createdAt: Instant = Clock.System.now(),
    val isSynced: Boolean = false
)