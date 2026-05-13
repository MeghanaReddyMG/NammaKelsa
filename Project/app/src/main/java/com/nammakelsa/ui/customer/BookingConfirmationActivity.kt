package com.nammakelsa.ui.customer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nammakelsa.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_confirmation)

        val bookingId = intent.getStringExtra("BOOKING_ID") ?: ""
        val workerName = intent.getStringExtra("WORKER_NAME") ?: ""
        val date = intent.getLongExtra("DATE", 0)
        val startTime = intent.getStringExtra("START_TIME") ?: ""
        val endTime = intent.getStringExtra("END_TIME") ?: ""
        val address = intent.getStringExtra("ADDRESS") ?: ""
        val price = intent.getDoubleExtra("PRICE", 0.0)

        findViewById<TextView>(R.id.tvBookingId).text = "Booking ID: #$bookingId"
        
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        val dateStr = sdf.format(Date(date))
        
        findViewById<TextView>(R.id.tvBookingDetails).text = """
            $workerName
            📅 $dateStr
            ⏰ $startTime - $endTime
            📍 $address
            💰 ₹$price
        """.trimIndent()

        findViewById<Button>(R.id.btnViewStatus).setOnClickListener {
            startActivity(Intent(this, com.nammakelsa.ui.common.BookingHistoryActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnBackToSearch).setOnClickListener {
            val intent = Intent(this, CustomerSearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
