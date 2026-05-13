package com.nammakelsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val skill: String,
    val experience: Int,
    val dailyRate: Double,
    val phone: String,
    val address: String,
    val bio: String,
    val profileImage: String,
    val workImages: List<String>,
    val isAvailable: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val latitude: Double,
    val longitude: Double
)
