package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val transactionRef: String,
    val playerId: String,
    val playerNickname: String,
    val serverRegion: String,
    val category: String,
    val itemName: String,
    val diamondCount: Int,
    val bonusDiamonds: Int,
    val price: Double,
    val discountAmount: Double,
    val promoCodeApplied: String?,
    val paymentMethod: String,
    val paymentStatus: String, // "COMPLETED", "PROCESSING", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)
