package org.example.aapanam.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String?,
    val address: String?,
    val createdAt: Instant = Clock.System.now(),
    val isSynced: Boolean = false
)
