package com.nammakelsa.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Booking
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.ui.common.BookingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkerDashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvWorkerInfo: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvAverageRating: TextView
    private lateinit var switchAvailability: MaterialSwitch
    private lateinit var rvRecentBookings: RecyclerView
    private lateinit var adapter: BookingAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: WorkerRepository
    private var isProgrammaticChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_dashboard)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvWorkerInfo = findViewById(R.id.tvWorkerInfo)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvAverageRating = findViewById(R.id.tvAverageRating)
        switchAvailability = findViewById(R.id.switchAvailability)
        rvRecentBookings = findViewById(R.id.rvRecentBookings)
        
        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao(), database.favoriteDao())

        rvRecentBookings.layoutManager = LinearLayoutManager(this)
        adapter = BookingAdapter(emptyList(), "worker", this::onBookingAction, this::onBookingDecline)
        rvRecentBookings.adapter = adapter

        findViewById<View>(R.id.btnViewAllBookings).setOnClickListener {
            startActivity(Intent(this, com.nammakelsa.ui.common.BookingHistoryActivity::class.java))
        }

        findViewById<View>(R.id.layoutReviews).setOnClickListener {
            startActivity(Intent(this, WorkerReviewsActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavWorker)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_worker_home -> true
                R.id.nav_worker_bookings -> {
                    startActivity(Intent(this, com.nammakelsa.ui.common.BookingHistoryActivity::class.java))
                    true
                }
                R.id.nav_worker_profile -> {
                    startActivity(Intent(this, WorkerProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val uid = auth.currentUser?.uid ?: ""
        if (uid.isNotEmpty()) {
            loadWorkerProfile(uid)
            loadTaskSummary(uid)
        }

        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticChange) return@setOnCheckedChangeListener
            repository.updateAvailability(uid, isChecked)
            Toast.makeText(this, if (isChecked) "You are now online" else "You are offline", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWorkerProfile(uid: String) {
        repository.getWorkerDetails(uid) { worker ->
            if (worker != null) {
                tvWelcome.text = "Welcome, ${worker.name}!"
                tvWorkerInfo.text = "${worker.name}\n${worker.skill}\n₹${worker.dailyRate}/day"
                tvAverageRating.text = String.format("%.1f", worker.rating)

                isProgrammaticChange = true
                switchAvailability.isChecked = worker.isAvailable
                isProgrammaticChange = false
            }
        }
    }

    private fun loadTaskSummary(uid: String) {
        repository.getWorkerBookings(uid) { bookings ->
            val pending = bookings.count { it.status == "pending" || it.status == "accepted" }
            val completed = bookings.count { it.status == "completed" }
            
            tvPendingCount.text = pending.toString()
            tvCompletedCount.text = completed.toString()
            
            val emptyMsg = findViewById<TextView>(R.id.tvBookingsEmpty)
            if (bookings.isEmpty()) {
                emptyMsg.visibility = View.VISIBLE
                rvRecentBookings.visibility = View.GONE
            } else {
                emptyMsg.visibility = View.GONE
                rvRecentBookings.visibility = View.VISIBLE
                // Show only top 3 most recent
                adapter.updateData(bookings.take(3))
            }
        }
    }

    private fun onBookingAction(booking: Booking) {
        if (booking.status == "pending") {
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
}
