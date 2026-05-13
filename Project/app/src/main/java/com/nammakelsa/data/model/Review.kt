package com.nammakelsa.data.model

data class Review(
  val id: String = "",
  val workerId: String = "",
  val customerId: String = "",
  val rating: Float = 0f,
  val comment: String = "",
  val timestamp: Long = 0
)
