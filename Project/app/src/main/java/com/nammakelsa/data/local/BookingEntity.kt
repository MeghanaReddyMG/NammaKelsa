package com.nammakelsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val workerId: String,
    val customerId: String,
    val date: Long,
    val startTime: String,
    val endTime: String,
    val address: String,
    val notes: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: Long
)
