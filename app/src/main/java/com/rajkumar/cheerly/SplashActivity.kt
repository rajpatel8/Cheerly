package com.rajkumar.cheerly
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay

/**
 * SplashActivity is the entry point of the application which displays a splash screen
 * for 2 seconds before navigating to the MainActivity.
 */
class SplashActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // giving a delay of 2 seconds before moving to the MainActivity
        lifecycleScope.launchWhenCreated {
            delay(2000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}

