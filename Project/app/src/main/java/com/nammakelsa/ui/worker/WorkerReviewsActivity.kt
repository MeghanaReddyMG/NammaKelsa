package com.nammakelsa.ui.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Review
import com.nammakelsa.data.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkerReviewsActivity : AppCompatActivity() {

    private lateinit var rvReviews: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private lateinit var repository: WorkerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_reviews)

        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao())

        rvReviews = findViewById(R.id.rvReviews)
        rvReviews.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(emptyList())
        rvReviews.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadReviews(uid)
    }

    private fun loadReviews(workerId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            repository.getReviews(workerId).collect { reviews ->
                adapter.updateData(reviews)
            }
        }
    }
}

class ReviewAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvReviewerName)
        val tvRating: TextView = view.findViewById(R.id.tvReviewRating)
        val tvComment: TextView = view.findViewById(R.id.tvReviewComment)
        val tvDate: TextView = view.findViewById(R.id.tvReviewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvName.text = "Customer: ${review.customerId.takeLast(6)}"
        holder.tvRating.text = "⭐ ${review.rating}"
        holder.tvComment.text = review.comment
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        holder.tvDate.text = sdf.format(Date(review.timestamp))
    }

    override fun getItemCount() = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
