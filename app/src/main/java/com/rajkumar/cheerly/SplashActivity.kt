package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            // Show splash for 2 seconds
            delay(2000)

            // Check if user has completed initial setup
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val isUserPreferenceSet = sharedPreferences.getBoolean("isUserPreferenceSet", false)

            // Determine which activity to launch
            val intent = when {
                !isUserPreferenceSet -> Intent(this@SplashActivity, UserPrefrence::class.java)
                !isServicesAuthenticated() -> Intent(this@SplashActivity, LoginActivity::class.java)
                else -> Intent(this@SplashActivity, MainActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }

    private fun isServicesAuthenticated(): Boolean {
        // This will be implemented once we create AuthManager
        // For now, always return false to force login flow
        return false
    }
}