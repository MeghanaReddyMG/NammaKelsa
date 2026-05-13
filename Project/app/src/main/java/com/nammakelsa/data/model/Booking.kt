package com.nammakelsa.data.model

data class Booking(
    val id: String = "",
    val workerId: String = "",
    val customerId: String = "",
    val date: Long = 0,
    val startTime: String = "",
    val endTime: String = "",
    val address: String = "",
    val notes: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "pending",  // pending, accepted, completed, cancelled
    val createdAt: Long = 0
)
