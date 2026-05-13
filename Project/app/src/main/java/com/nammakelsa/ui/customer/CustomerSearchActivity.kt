package com.nammakelsa.ui.customer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.Worker
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.data.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CustomerSearchActivity : AppCompatActivity() {

    private lateinit var rvWorkers: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tvCustomerGreeting: TextView
    private lateinit var repository: WorkerRepository
    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth
    private var workerList = mutableListOf<Worker>()
    private lateinit var adapter: WorkerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_search)

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        repository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao())
        userRepository = UserRepository(auth, database.userDao())

        tvCustomerGreeting = findViewById(R.id.tvCustomerGreeting)
        etSearch = findViewById(R.id.etSearch)
        rvWorkers = findViewById(R.id.rvWorkers)

        rvWorkers.layoutManager = LinearLayoutManager(this)
        adapter = WorkerAdapter(workerList, this::onCallWorker, this::onBookWorker, this::onWorkerClick)
        rvWorkers.adapter = adapter

        findViewById<View>(R.id.btnMap).setOnClickListener {
            startActivity(Intent(this, com.nammakelsa.ui.common.MapActivity::class.java))
        }

        findViewById<View>(R.id.btnFilter).setOnClickListener {
            Toast.makeText(this, "Filter implemented soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnMyBookings).setOnClickListener {
            startActivity(Intent(this, com.nammakelsa.ui.common.BookingHistoryActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> true
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.nav_bookings -> {
                    startActivity(Intent(this, com.nammakelsa.ui.common.BookingHistoryActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, com.nammakelsa.ui.common.MapActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, CustomerProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadCustomerProfile()
        loadWorkers()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterWorkers(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadCustomerProfile() {
        val uid = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepository.getUserSync(uid)
            if (user != null) {
                tvCustomerGreeting.text = "Hello, ${user.name}"
            }
        }
    }

    private fun loadWorkers() {
        repository.getAllAvailableWorkersRealtime { workers ->
            workerList.clear()
            workerList.addAll(workers)
            filterWorkers(etSearch.text.toString())
        }
    }

    private fun filterWorkers(query: String) {
        val filtered = if (query.isEmpty()) {
            workerList
        } else {
            workerList.filter {
                it.skill.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(filtered)
    }

    private fun onCallWorker(worker: Worker) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91${worker.phone}"))
        startActivity(intent)
    }

    private fun onBookWorker(worker: Worker) {
        val intent = Intent(this, com.nammakelsa.ui.customer.BookingActivity::class.java)
        intent.putExtra("WORKER_ID", worker.uid)
        startActivity(intent)
    }

    private fun onWorkerClick(worker: Worker) {
        val intent = Intent(this, com.nammakelsa.ui.customer.WorkerDetailsActivity::class.java)
        intent.putExtra("WORKER_ID", worker.uid)
        startActivity(intent)
    }
}

class WorkerAdapter(
    private var workers: List<Worker>,
    private val onCallClick: (Worker) -> Unit,
    private val onBookClick: (Worker) -> Unit,
    private val onItemClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvWorkerName)
        val tvSkill: TextView = view.findViewById(R.id.tvWorkerSkill)
        val tvRate: TextView = view.findViewById(R.id.tvWorkerRate)
        val tvRating: TextView = view.findViewById(R.id.tvWorkerRating)
        val btnCall: Button = view.findViewById(R.id.btnCall)
        val btnBook: Button = view.findViewById(R.id.btnBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]
        holder.tvName.text = worker.name
        holder.tvSkill.text = worker.skill
        holder.tvRate.text = "₹${worker.dailyRate}/day"
        holder.tvRating.text = "⭐ ${worker.rating} (${worker.reviewCount} reviews)"
        
        holder.btnCall.setOnClickListener { onCallClick(worker) }
        holder.btnBook.setOnClickListener { onBookClick(worker) }
        holder.itemView.setOnClickListener { onItemClick(worker) }
    }

    override fun getItemCount() = workers.size

    fun updateData(newWorkers: List<Worker>) {
        workers = newWorkers
        notifyDataSetChanged()
    }
}
