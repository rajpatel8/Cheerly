package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

// Main Activity Class
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isUserPreferenceSet = sharedPreferences.getBoolean("isUserPreferenceSet", false)

        if (isUserPreferenceSet) {
            // Redirect to PromptActivity if user input is already set
            startActivity(Intent(this, PromptActivity::class.java))
        } else {
            // Redirect to UserPreference if it's the first time
            startActivity(Intent(this, UserPrefrence::class.java))
        }
    }
}





