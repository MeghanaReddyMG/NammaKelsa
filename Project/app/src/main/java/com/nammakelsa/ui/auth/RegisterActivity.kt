package com.nammakelsa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.User
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.ui.customer.CustomerSearchActivity
import com.nammakelsa.ui.worker.WorkerProfileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        userType = intent.getStringExtra("USER_TYPE")

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    saveUserAndNavigate(uid, username, email, phone)
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun saveUserAndNavigate(uid: String, name: String, email: String, phone: String) {
        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(auth, database.userDao())

        val user = User(
            uid = uid,
            phone = phone,
            userType = userType ?: "customer",
            name = name,
            address = ""
        )

        CoroutineScope(Dispatchers.IO).launch {
            userRepository.saveUserLocally(user)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                if (userType == "worker") {
                    startActivity(Intent(this@RegisterActivity, WorkerProfileActivity::class.java))
                } else {
                    startActivity(Intent(this@RegisterActivity, CustomerSearchActivity::class.java))
                }
                finishAffinity()
            }
        }
    }
}
