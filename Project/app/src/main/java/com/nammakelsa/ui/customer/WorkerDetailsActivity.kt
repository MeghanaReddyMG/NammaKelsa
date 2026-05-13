package com.nammakelsa.ui.customer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.ui.common.PortfolioAdapter

class WorkerDetailsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: WorkerRepository
    private lateinit var portfolioAdapter: PortfolioAdapter
    private var workerPhone: String? = null
    private var workerId: String? = null
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_details)

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(
            database.workerDao(),
            database.bookingDao(),
            database.reviewDao(),
            database.favoriteDao()
        )
        workerId = intent.getStringExtra("WORKER_ID")

        if (workerId == null) {
            Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvSkill = findViewById<TextView>(R.id.tvSkill)
        val tvRating = findViewById<TextView>(R.id.tvRating)
        val tvRate = findViewById<TextView>(R.id.tvRate)
        val tvExperience = findViewById<TextView>(R.id.tvExperience)
        val tvAddress = findViewById<TextView>(R.id.tvAddress)
        val tvBio = findViewById<TextView>(R.id.tvBio)
        val rvPortfolio = findViewById<RecyclerView>(R.id.rvPortfolio)
        val btnCall = findViewById<MaterialButton>(R.id.btnCall)
        val btnBook = findViewById<MaterialButton>(R.id.btnBook)
        val btnFav = findViewById<MaterialButton>(R.id.btnFav)

        portfolioAdapter = PortfolioAdapter(emptyList())
        rvPortfolio.adapter = portfolioAdapter

        repository.getWorkerDetails(workerId!!) { worker ->
            if (worker != null) {
                tvName.text = worker.name
                tvSkill.text = worker.skill
                tvRating.text = "⭐ ${worker.rating} (${worker.reviewCount} reviews)"
                tvRate.text = "₹${worker.dailyRate}/day"
                tvExperience.text = "${worker.experience} years experience"
                tvAddress.text = worker.address
                tvBio.text = if (worker.bio.isNotEmpty()) worker.bio else "No bio available"
                portfolioAdapter.updateData(worker.workImages)
                workerPhone = worker.phone
            } else {
                Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        }

        // Check if favorite
        val userId = auth.currentUser?.uid
        if (userId != null) {
            repository.isFavorite(userId, workerId!!) { fav ->
                isFavorite = fav
                updateFavoriteIcon(btnFav)
            }
        }

        btnCall.setOnClickListener {
            workerPhone?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91$phone"))
                startActivity(intent)
            }
        }

        btnBook.setOnClickListener {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("WORKER_ID", workerId)
            startActivity(intent)
        }

        btnFav.setOnClickListener {
            if (userId == null) {
                Toast.makeText(this, "Please login to favorite", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            repository.toggleFavorite(userId, workerId!!) { fav ->
                isFavorite = fav
                updateFavoriteIcon(btnFav)
                val msg = if (isFavorite) "Added to favorites" else "Removed from favorites"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteIcon(btnFav: MaterialButton) {
        if (isFavorite) {
            btnFav.setIconResource(R.drawable.ic_heart_filled)
        } else {
            btnFav.setIconResource(R.drawable.ic_heart)
        }
    }
}
