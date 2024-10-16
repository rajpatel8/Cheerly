package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

// Main Activity Class
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.user_prefrence)
        // open the UserPreferenceActivity
        startActivity(Intent(this, UserPrefrence::class.java))

    }

    }





