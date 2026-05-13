package com.nammakelsa.ui.customer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Worker
import com.nammakelsa.data.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var adapter: WorkerAdapter
    private lateinit var repository: WorkerRepository
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(
            database.workerDao(),
            database.bookingDao(),
            database.reviewDao(),
            database.favoriteDao()
        )

        rvFavorites = findViewById(R.id.rvFavorites)
        rvFavorites.layoutManager = LinearLayoutManager(this)
        adapter = WorkerAdapter(emptyList(), this::onCallWorker, this::onBookWorker, this::onWorkerClick)
        rvFavorites.adapter = adapter

        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                repository.getFavoriteWorkers(userId).collect { workers ->
                    adapter.updateData(workers)
                }
            }
        } else {
            Toast.makeText(this, "Please login to see favorites", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun onCallWorker(worker: Worker) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${worker.phone}"))
        startActivity(intent)
    }

    private fun onBookWorker(worker: Worker) {
        val intent = Intent(this, BookingActivity::class.java)
        intent.putExtra("WORKER_ID", worker.uid)
        startActivity(intent)
    }

    private fun onWorkerClick(worker: Worker) {
        val intent = Intent(this, WorkerDetailsActivity::class.java)
        intent.putExtra("WORKER_ID", worker.uid)
        startActivity(intent)
    }
}
