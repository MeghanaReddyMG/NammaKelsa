package com.nammakelsa.data.model

data class Worker(
  val uid: String = "",
  val name: String = "",
  val skill: String = "",
  val experience: Int = 0,
  val dailyRate: Double = 0.0,
  val phone: String = "",
  val address: String = "",
  val bio: String = "",
  val profileImage: String = "",
  val workImages: List<String> = emptyList(),
  val isAvailable: Boolean = false,
  val rating: Double = 0.0,
  val reviewCount: Int = 0,
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
)
