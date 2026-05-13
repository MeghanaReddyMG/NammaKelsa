package com.nammakelsa.ui.worker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.mapper.toEntity
import com.nammakelsa.data.mapper.toModel
import com.nammakelsa.data.model.User
import com.nammakelsa.data.model.Worker
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.data.repository.WorkerRepository
import com.nammakelsa.ui.auth.RoleSelectionActivity
import com.nammakelsa.ui.common.PortfolioAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WorkerProfileActivity : AppCompatActivity() {

    private val TAG = "WorkerProfileActivity"
    private lateinit var userRepository: UserRepository
    private lateinit var workerRepository: WorkerRepository
    private lateinit var portfolioAdapter: PortfolioAdapter
    private var existingWorker: Worker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(FirebaseAuth.getInstance(), database.userDao())
        workerRepository = WorkerRepository(database.workerDao(), database.bookingDao(), database.reviewDao(), database.favoriteDao())

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etRate = findViewById<TextInputEditText>(R.id.etRate)
        val etExperience = findViewById<TextInputEditText>(R.id.etExperience)
        val etAddress = findViewById<TextInputEditText>(R.id.etAddress)
        val etBio = findViewById<TextInputEditText>(R.id.etBio)
        val rvPortfolio = findViewById<RecyclerView>(R.id.rvPortfolio)
        val btnAddImage = findViewById<MaterialButton>(R.id.btnAddImage)
        val spinnerSkill = findViewById<Spinner>(R.id.spinnerSkill)

        portfolioAdapter = PortfolioAdapter(emptyList())
        rvPortfolio.adapter = portfolioAdapter

        btnAddImage.setOnClickListener {
            Toast.makeText(this, "Image Picker coming soon!", Toast.LENGTH_SHORT).show()
        }
        val btnSaveProfile = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        val skills = arrayOf("Electrician", "Plumber", "Carpenter", "Painter", "Mason", "Gardener", "AC Technician", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, skills)
        spinnerSkill.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            btnLogout.visibility = View.VISIBLE
            loadExistingProfile(uid, etName, etRate, etExperience, etAddress, etBio, spinnerSkill, skills)
        }

        btnSaveProfile.setOnClickListener {
            val name = etName.text.toString().trim()
            val rateStr = etRate.text.toString().trim()
            val expStr = etExperience.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val bio = etBio.text.toString().trim()
            val skill = spinnerSkill.selectedItem.toString()

            if (name.isEmpty() || rateStr.isEmpty() || expStr.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rate = rateStr.toDoubleOrNull() ?: 0.0
            val exp = expStr.toIntOrNull() ?: 0
            
            val auth = FirebaseAuth.getInstance()
            val firebaseUser = auth.currentUser
            val currentUid = firebaseUser?.uid ?: "demo_worker_${System.currentTimeMillis()}"
            val phone = firebaseUser?.phoneNumber ?: "0000000000"

            val user = User(
                uid = currentUid,
                phone = phone,
                userType = "worker",
                name = name,
                address = address
            )

            val worker = existingWorker?.copy(
                name = name,
                skill = skill,
                experience = exp,
                dailyRate = rate,
                address = address,
                bio = bio
            ) ?: Worker(
                uid = currentUid,
                name = name,
                skill = skill,
                experience = exp,
                dailyRate = rate,
                phone = phone,
                address = address,
                bio = bio,
                isAvailable = true,
                rating = 5.0,
                reviewCount = 0,
                latitude = 12.9716,
                longitude = 77.5946
            )

            btnSaveProfile.isEnabled = false
            btnSaveProfile.text = "Saving..."

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    userRepository.saveUserLocally(user)
                    database.workerDao().insertWorker(worker.toEntity())
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@WorkerProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        if (existingWorker == null) {
                            val intent = Intent(this@WorkerProfileActivity, WorkerDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        finish()
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        handleError("Save failed", e, btnSaveProfile)
                    }
                }
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadExistingProfile(
        uid: String,
        etName: TextInputEditText,
        etRate: TextInputEditText,
        etExperience: TextInputEditText,
        etAddress: TextInputEditText,
        etBio: TextInputEditText,
        spinnerSkill: Spinner,
        skills: Array<String>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepository.getUserSync(uid)
            if (user != null) {
                etName.setText(user.name)
                etAddress.setText(user.address)
            }

            val database = AppDatabase.getDatabase(this@WorkerProfileActivity)
            val workerEntity = database.workerDao().getWorker(uid).first()
            if (workerEntity != null) {
                existingWorker = workerEntity.toModel()
                existingWorker?.let {
                    etName.setText(it.name)
                    etRate.setText(it.dailyRate.toString())
                    etExperience.setText(it.experience.toString())
                    etAddress.setText(it.address)
                    etBio.setText(it.bio)
                    portfolioAdapter.updateData(it.workImages)
                    val skillIndex = skills.indexOf(it.skill)
                    if (skillIndex >= 0) {
                        spinnerSkill.setSelection(skillIndex)
                    }
                }
            }
        }
    }

    private fun handleError(message: String, e: Exception, button: MaterialButton) {
        button.isEnabled = true
        button.text = "SAVE CHANGES"
        Log.e(TAG, "$message: ${e.message}")
        Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
