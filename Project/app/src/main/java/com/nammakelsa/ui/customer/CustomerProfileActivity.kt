package com.nammakelsa.ui.customer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.User
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.ui.auth.RoleSelectionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomerProfileActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)

        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(auth, database.userDao())

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etAddress = findViewById<TextInputEditText>(R.id.etAddress)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadProfile(currentUser.uid, etName, etPhone, etAddress)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(
                uid = currentUser?.uid ?: "",
                phone = phone,
                userType = "customer",
                name = name,
                address = address
            )

            CoroutineScope(Dispatchers.IO).launch {
                userRepository.saveUserLocally(user)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@CustomerProfileActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfile(uid: String, etName: TextInputEditText, etPhone: TextInputEditText, etAddress: TextInputEditText) {
        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepository.getUserSync(uid)
            if (user != null) {
                etName.setText(user.name)
                etPhone.setText(user.phone)
                etAddress.setText(user.address)
            }
        }
    }
}
