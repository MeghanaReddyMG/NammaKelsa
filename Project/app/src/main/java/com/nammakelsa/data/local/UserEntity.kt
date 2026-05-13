package com.nammakelsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val phone: String,
    val userType: String,
    val name: String,
    val address: String,
    val profileImage: String
)
