package com.nammakelsa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nammakelsa.R
import com.nammakelsa.data.local.AppDatabase
import com.nammakelsa.data.repository.UserRepository
import com.nammakelsa.ui.customer.CustomerSearchActivity
import com.nammakelsa.ui.worker.WorkerDashboardActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoleSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val database = AppDatabase.getDatabase(this)
            val userRepository = UserRepository(auth, database.userDao())
            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserSync(auth.currentUser!!.uid)
                if (user != null) {
                    if (user.userType == "worker") {
                        startActivity(Intent(this@RoleSelectionActivity, WorkerDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this@RoleSelectionActivity, CustomerSearchActivity::class.java))
                    }
                    finish()
                }
            }
        }

        setContentView(R.layout.activity_role_selection)

        findViewById<View>(R.id.cardWorker).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("USER_TYPE", "worker")
            startActivity(intent)
        }

        findViewById<View>(R.id.cardCustomer).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("USER_TYPE", "customer")
            startActivity(intent)
        }

        findViewById<View>(R.id.tvLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
