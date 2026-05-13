package com.nammakelsa.ui.customer

import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Review
import com.nammakelsa.data.repository.WorkerRepository

class AddReviewActivity : AppCompatActivity() {

    private lateinit var repository: WorkerRepository
    private var workerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        workerId = intent.getStringExtra("WORKER_ID") ?: ""
        if (workerId.isEmpty()) {
            finish()
            return
        }

        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao())

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<TextInputEditText>(R.id.etComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReview)

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etComment.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val review = Review(
                id = "REV${System.currentTimeMillis()}",
                workerId = workerId,
                customerId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )

            repository.addReview(review) { success ->
                if (success) {
                    Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
