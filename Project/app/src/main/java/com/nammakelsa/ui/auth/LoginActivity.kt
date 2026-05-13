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
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.ui.customer.CustomerSearchActivity
import com.nammakelsa.ui.worker.WorkerDashboardActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        userType = intent.getStringExtra("USER_TYPE")

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserProfileAndRoute()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("USER_TYPE", userType)
            startActivity(intent)
        }
    }

    private fun checkUserProfileAndRoute() {
        val uid = auth.currentUser?.uid ?: return
        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(auth, database.userDao())

        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepository.getUserSync(uid)
            if (user != null) {
                if (user.userType == "worker") {
                    startActivity(Intent(this@LoginActivity, WorkerDashboardActivity::class.java))
                } else {
                    startActivity(Intent(this@LoginActivity, CustomerSearchActivity::class.java))
                }
                finishAffinity()
            } else {
                // If user authenticated but no local profile, assume they need to set it up
                // (though register should have handled this)
                Toast.makeText(this@LoginActivity, "Profile not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
