package com.nammakelsa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import java.util.concurrent.TimeUnit
import com.nammakelsa.R
import com.nammakelsa.ui.worker.WorkerDashboardActivity
import com.nammakelsa.ui.customer.CustomerSearchActivity
import com.nammakelsa.ui.worker.WorkerProfileActivity
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.model.User
import com.nammakelsa.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OTPActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var phoneInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var otpSection: LinearLayout

    private var verificationId: String = ""
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        
        userType = intent.getStringExtra("USER_TYPE")
        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(auth, database.userDao())

        phoneInput = findViewById(R.id.phoneInput)
        otpInput = findViewById(R.id.otpInput)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        otpSection = findViewById(R.id.otpSection)

        findViewById<Button>(R.id.btnDemoLogin).setOnClickListener {
            Toast.makeText(this, "Entering Demo Mode...", Toast.LENGTH_SHORT).show()
            // If anonymous auth is not enabled, we still want the demo to work
            auth.signInAnonymously().addOnCompleteListener { 
                checkUserProfileAndRoute() 
            }
        }

        sendOtpButton.setOnClickListener {
            val phone = phoneInput.text.toString().trim()
            if (phone.length != 10) {
                Toast.makeText(this, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Fast-track for specific numbers if you want to bypass OTP
            if (phone == "1234567890") {
                checkUserProfileAndRoute()
                return@setOnClickListener
            }
            sendOTP("+91$phone")
        }

        verifyOtpButton.setOnClickListener {
            val code = otpInput.text.toString().trim()
            if (code.length != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (verificationId.isEmpty()) {
                Toast.makeText(this, "Please send OTP first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithCredential(credential)
        }
    }

    private fun sendOTP(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    Toast.makeText(this@OTPActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                    // Allow manual entry if auto-verification fails
                    otpSection.visibility = View.VISIBLE
                    sendOtpButton.visibility = View.GONE
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    otpSection.visibility = View.VISIBLE
                    sendOtpButton.visibility = View.GONE
                    Toast.makeText(this@OTPActivity, "OTP sent!", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserProfileAndRoute()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserProfileAndRoute() {
        val uid = auth.currentUser?.uid ?: "demo_user_${System.currentTimeMillis()}"
        
        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepository.getUserSync(uid)
            if (user != null) {
                if (user.userType == "worker") {
                    startActivity(Intent(this@OTPActivity, WorkerDashboardActivity::class.java))
                } else {
                    startActivity(Intent(this@OTPActivity, CustomerSearchActivity::class.java))
                }
            } else {
                if (userType == "worker") {
                    startActivity(Intent(this@OTPActivity, WorkerProfileActivity::class.java))
                } else {
                    saveBasicCustomerProfile(uid)
                }
            }
            finish()
        }
    }

    private fun saveBasicCustomerProfile(uid: String) {
        val user = User(
            uid = uid,
            phone = (auth.currentUser?.phoneNumber ?: ""),
            userType = "customer",
            name = "Demo Customer",
            address = ""
        )
        userRepository.saveUserLocally(user)
        startActivity(Intent(this, CustomerSearchActivity::class.java))
        finish()
    }
}
