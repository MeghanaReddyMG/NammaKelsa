package com.nammakelsa.data.mapper

import com.nammakelsa.data.local.BookingEntity
import com.nammakelsa.data.local.ReviewEntity
import com.nammakelsa.data.local.UserEntity
import com.nammakelsa.data.local.WorkerEntity
import com.nammakelsa.data.model.Booking
import com.nammakelsa.data.model.Review
import com.nammakelsa.data.model.User
import com.nammakelsa.data.model.Worker

fun UserEntity.toModel() = User(uid, phone, userType, name, address, profileImage)
fun User.toEntity() = UserEntity(uid, phone, userType, name, address, profileImage)

fun WorkerEntity.toModel() = Worker(uid, name, skill, experience, dailyRate, phone, address, bio, profileImage, workImages, isAvailable, rating, reviewCount, latitude, longitude)
fun Worker.toEntity() = WorkerEntity(uid, name, skill, experience, dailyRate, phone, address, bio, profileImage, workImages, isAvailable, rating, reviewCount, latitude, longitude)

fun BookingEntity.toModel() = Booking(id, workerId, customerId, date, startTime, endTime, address, notes, totalPrice, status, createdAt)
fun Booking.toEntity() = BookingEntity(id, workerId, customerId, date, startTime, endTime, address, notes, totalPrice, status, createdAt)

fun ReviewEntity.toModel() = Review(id, workerId, customerId, rating, comment, timestamp)
fun Review.toEntity() = ReviewEntity(id, workerId, customerId, rating, comment, timestamp)
