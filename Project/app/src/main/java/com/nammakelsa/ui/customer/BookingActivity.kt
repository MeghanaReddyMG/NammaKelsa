package com.nammakelsa.ui.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Booking
import com.nammakelsa.data.model.Worker
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.data.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: WorkerRepository
    private lateinit var userRepository: UserRepository
    private var workerId: String? = null
    private var workerName: String = ""
    private var dailyRate: Double = 0.0

    private var selectedDate: String = ""
    private var startTime: String = ""
    private var endTime: String = ""

    private var startHour: Int = -1
    private var startMinute: Int = -1
    private var endHour: Int = -1
    private var endMinute: Int = -1

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao())
        userRepository = UserRepository(auth, database.userDao())
        workerId = intent.getStringExtra("WORKER_ID")

        if (workerId == null) {
            Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvWorkerSummary = findViewById<TextView>(R.id.tvWorkerSummary)
        val btnDate = findViewById<Button>(R.id.btnDate)
        val btnStartTime = findViewById<Button>(R.id.btnStartTime)
        val btnEndTime = findViewById<Button>(R.id.btnEndTime)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Fetch worker summary
        repository.getWorkerDetails(workerId!!) { worker ->
            if (worker != null) {
                workerName = worker.name
                tvWorkerSummary.text = "${worker.name}\n${worker.skill} • ₹${worker.dailyRate}/day\n⭐ ${worker.rating}"
                dailyRate = worker.dailyRate
                updatePrice(tvTotalPrice)
            }
        }

        // Fetch user address to prefill
        val customerId = auth.currentUser?.uid
        if (customerId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserSync(customerId)
                if (user != null) {
                    etAddress.setText(user.address)
                }
            }
        }

        btnDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
                selectedDate = sdf.format(calendar.time)
                btnDate.text = selectedDate
            }
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnStartTime.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                startHour = hourOfDay
                startMinute = minute
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                startTime = sdf.format(cal.time)
                btnStartTime.text = startTime
                updatePrice(tvTotalPrice)
            }
            TimePickerDialog(this, timeSetListener, 9, 0, false).show()
        }

        btnEndTime.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                endHour = hourOfDay
                endMinute = minute
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                endTime = sdf.format(cal.time)
                btnEndTime.text = endTime
                updatePrice(tvTotalPrice)
            }
            TimePickerDialog(this, timeSetListener, 17, 0, false).show()
        }

        btnConfirm.setOnClickListener {
            if (selectedDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || etAddress.text.toString().isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (calculateHours() <= 0) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = Booking(
                id = "", // Will be set by repository/firestore
                workerId = workerId!!,
                customerId = customerId ?: "",
                date = calendar.timeInMillis,
                startTime = startTime,
                endTime = endTime,
                address = etAddress.text.toString(),
                notes = etNotes.text.toString(),
                totalPrice = calculatePrice(),
                status = "pending",
                createdAt = System.currentTimeMillis()
            )

            repository.createBooking(booking) { success ->
                if (success) {
                    val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
                        putExtra("BOOKING_ID", "BK${System.currentTimeMillis().toString().takeLast(6)}") // Simplified ID for display
                        putExtra("WORKER_NAME", workerName)
                        putExtra("DATE", booking.date)
                        putExtra("START_TIME", booking.startTime)
                        putExtra("END_TIME", booking.endTime)
                        putExtra("ADDRESS", booking.address)
                        putExtra("PRICE", booking.totalPrice)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancel.setOnClickListener { finish() }
    }

    private fun updatePrice(tvTotalPrice: TextView) {
        val hours = calculateHours()
        if (hours > 0) {
            val total = dailyRate * hours
            tvTotalPrice.text = "₹$total ($hours hours × ₹$dailyRate)"
        } else {
            tvTotalPrice.text = "₹0"
        }
    }

    private fun calculateHours(): Double {
        if (startHour == -1 || endHour == -1) return 0.0
        val startInMinutes = startHour * 60 + startMinute
        val endInMinutes = endHour * 60 + endMinute
        val diff = endInMinutes - startInMinutes
        return if (diff > 0) diff / 60.0 else 0.0
    }

    private fun calculatePrice(): Double {
        return dailyRate * calculateHours()
    }
}
