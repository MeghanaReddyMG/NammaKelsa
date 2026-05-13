package com.nammakelsa.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Booking
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.ui.customer.AddReviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var repository: WorkerRepository
    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: BookingAdapter
    private var userType: String = "customer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao(), database.favoriteDao())
        userRepository = UserRepository(auth, database.userDao())
        
        rvBookings = findViewById(R.id.rvBookings)
        rvBookings.layoutManager = LinearLayoutManager(this)
        
        val uid = auth.currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepository.getUserSync(uid)
            userType = user?.userType ?: "customer"
            
            adapter = BookingAdapter(
                emptyList(), 
                userType, 
                this@BookingHistoryActivity::onBookingAction, 
                this@BookingHistoryActivity::onBookingDecline,
                this@BookingHistoryActivity::onBookingRate
            )
            rvBookings.adapter = adapter

            if (userType == "worker") {
                repository.getWorkerBookings(uid) { bookings ->
                    adapter.updateData(bookings)
                }
            } else {
                repository.getCustomerBookings(uid) { bookings ->
                    adapter.updateData(bookings)
                }
            }
        }
    }

    private fun onBookingAction(booking: Booking) {
        if (userType == "worker" && booking.status == "pending") {
            repository.updateBookingStatus(booking.id, "accepted") { success ->
                if (success) Toast.makeText(this, "Booking Accepted", Toast.LENGTH_SHORT).show()
            }
        } else if (booking.status == "accepted") {
            repository.updateBookingStatus(booking.id, "completed") { success ->
                if (success) Toast.makeText(this, "Booking Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onBookingDecline(booking: Booking) {
        repository.updateBookingStatus(booking.id, "cancelled") { success ->
            if (success) Toast.makeText(this, "Booking Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onBookingRate(booking: Booking) {
        val intent = Intent(this, AddReviewActivity::class.java)
        intent.putExtra("WORKER_ID", booking.workerId)
        startActivity(intent)
    }
}
