package com.nammakelsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // favorite_{userId}_{workerId}
    val userId: String,
    val workerId: String,
    val addedAt: Long
)
