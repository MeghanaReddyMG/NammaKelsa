package com.nammakelsa.data.repository

import com.nammakelsa.data.local.BookingDao
import com.nammakelsa.data.local.FavoriteDao
import com.nammakelsa.data.local.FavoriteEntity
import com.nammakelsa.data.local.ReviewDao
import com.nammakelsa.data.local.WorkerDao
import com.nammakelsa.data.mapper.toEntity
import com.nammakelsa.data.mapper.toModel
import com.nammakelsa.data.model.Booking
import com.nammakelsa.data.model.Review
import com.nammakelsa.data.model.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkerRepository(
    private val workerDao: WorkerDao,
    private val bookingDao: BookingDao,
    private val reviewDao: ReviewDao,
    private val favoriteDao: FavoriteDao? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun searchWorkersBySkillRealtime(
        skill: String,
        callback: (List<Worker>) -> Unit,
        errorCallback: (Exception) -> Unit
    ) {
        scope.launch {
            try {
                workerDao.getAvailableWorkersBySkill(skill).collect { entities ->
                    withContext(Dispatchers.Main) {
                        callback(entities.map { it.toModel() })
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorCallback(e)
                }
            }
        }
    }

    fun getAllAvailableWorkersRealtime(callback: (List<Worker>) -> Unit) {
        scope.launch {
            workerDao.getAllAvailableWorkers().collect { entities ->
                withContext(Dispatchers.Main) {
                    callback(entities.map { it.toModel() })
                }
            }
        }
    }

    fun updateAvailability(workerId: String, isAvailable: Boolean) {
        scope.launch {
            val worker = workerDao.getWorker(workerId).first()
            worker?.let {
                workerDao.insertWorker(it.copy(isAvailable = isAvailable))
            }
        }
    }

    fun getWorkerDetails(workerId: String, callback: (Worker?) -> Unit) {
        scope.launch {
            val worker = workerDao.getWorker(workerId).first()
            withContext(Dispatchers.Main) {
                callback(worker?.toModel())
            }
        }
    }

    fun getWorkerBookings(workerId: String, callback: (List<Booking>) -> Unit) {
        scope.launch {
            bookingDao.getBookingsForWorker(workerId).collect { entities ->
                withContext(Dispatchers.Main) {
                    callback(entities.map { it.toModel() })
                }
            }
        }
    }

    fun getCustomerBookings(customerId: String, callback: (List<Booking>) -> Unit) {
        scope.launch {
            bookingDao.getBookingsForCustomer(customerId).collect { entities ->
                withContext(Dispatchers.Main) {
                    callback(entities.map { it.toModel() })
                }
            }
        }
    }

    fun updateBookingStatus(bookingId: String, status: String, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                bookingDao.updateBookingStatus(bookingId, status)
                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    fun createBooking(booking: Booking, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                val finalBooking = if (booking.id.isEmpty()) {
                    booking.copy(id = "BK${System.currentTimeMillis()}")
                } else {
                    booking
                }
                bookingDao.insertBooking(finalBooking.toEntity())
                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    // Favorite operations
    fun toggleFavorite(userId: String, workerId: String, callback: (Boolean) -> Unit) {
        scope.launch {
            val favId = "favorite_${userId}_$workerId"
            val isFav = favoriteDao?.isFavorite(favId) ?: false
            if (isFav) {
                favoriteDao?.deleteFavorite(favId)
                withContext(Dispatchers.Main) { callback(false) }
            } else {
                favoriteDao?.insertFavorite(FavoriteEntity(favId, userId, workerId, System.currentTimeMillis()))
                withContext(Dispatchers.Main) { callback(true) }
            }
        }
    }

    fun isFavorite(userId: String, workerId: String, callback: (Boolean) -> Unit) {
        scope.launch {
            val favId = "favorite_${userId}_$workerId"
            val isFav = favoriteDao?.isFavorite(favId) ?: false
            withContext(Dispatchers.Main) { callback(isFav) }
        }
    }

    fun getFavoriteWorkers(userId: String): Flow<List<Worker>> {
        return (favoriteDao?.getFavoritesForUser(userId) ?: kotlinx.coroutines.flow.flowOf(emptyList())).map { favorites ->
            favorites.mapNotNull { fav ->
                workerDao.getWorker(fav.workerId).first()?.toModel()
            }
        }
    }

    fun getReviews(workerId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForWorker(workerId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun addReview(review: Review, callback: (Boolean) -> Unit) {
        scope.launch {
            try {
                reviewDao.insertReview(review.toEntity())
                updateWorkerRating(review.workerId)
                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    private suspend fun updateWorkerRating(workerId: String) {
        val reviews = reviewDao.getReviewsForWorker(workerId).first()
        val ratings = reviews.map { it.rating.toDouble() }
        val avgRating = if (ratings.isNotEmpty()) ratings.average() else 0.0
        
        val worker = workerDao.getWorker(workerId).first()
        worker?.let {
            workerDao.insertWorker(it.copy(
                rating = avgRating,
                reviewCount = reviews.size
            ))
        }
    }
}
