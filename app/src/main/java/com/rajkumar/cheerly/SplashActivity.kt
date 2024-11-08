package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import org.json.JSONException

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            // Show splash for 2 seconds
            delay(2000)

            // Check if user has completed initial setup
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            // Use this flag
            // - true to open the login page
            // - false to open the user preference page
            val isUserPreferenceSet = sharedPreferences.getBoolean("isUserPreferenceSet", true)

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
        // Check Spotify authentication
        val spotifyAuthState = getSpotifyAuthState()
        val isSpotifyAuthenticated = spotifyAuthState?.isAuthorized == true

        // Check YouTube/Google authentication
        val isGoogleAuthenticated = GoogleSignIn.getLastSignedInAccount(this) != null

        // Return true only if both services are authenticated
        return isSpotifyAuthenticated && isGoogleAuthenticated
    }

    private fun getSpotifyAuthState(): AuthState? {
        val prefs = getSharedPreferences("SpotifyAuthPrefs", MODE_PRIVATE)
        val jsonString = prefs.getString("auth_state", null)
        return if (jsonString != null) {
            try {
                AuthState.jsonDeserialize(jsonString)
            } catch (e: JSONException) {
                null
            }
        } else {
            null
        }
    }
}