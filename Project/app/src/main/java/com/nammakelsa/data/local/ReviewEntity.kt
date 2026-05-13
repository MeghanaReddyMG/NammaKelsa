package com.nammakelsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val workerId: String,
    val customerId: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long
)
