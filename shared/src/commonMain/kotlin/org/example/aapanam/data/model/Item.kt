package org.example.aapanam.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Item(
    val id: Long = 0,
    val name: String,
    val category: String? = null,
    val stockQuantity: Int,
    val costPrice: Double,
    val sellingPrice: Double,
    val imageUrl: String? = null,
    @SerialName("isSynced")
    val isSynced: Boolean = false,
    val createdAt: Long,
    val lastModified: Instant = Clock.System.now(),
)