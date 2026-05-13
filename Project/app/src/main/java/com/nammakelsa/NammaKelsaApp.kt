package com.nammakelsa

import android.app.Application
import com.google.firebase.FirebaseApp

class NammaKelsaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
