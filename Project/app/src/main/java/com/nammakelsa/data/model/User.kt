package com.nammakelsa.data.model

data class User(
  val uid: String = "",
  val phone: String = "",
  val userType: String = "",  // "worker" or "customer"
  val name: String = "",
  val address: String = "",
  val profileImage: String = ""
)
