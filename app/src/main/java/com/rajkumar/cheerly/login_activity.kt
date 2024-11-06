package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.rajkumar.cheerly.auth.SpotifyAuthManager
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import org.json.JSONException

class LoginActivity : ComponentActivity() {
    private lateinit var btnSpotifyLogin: MaterialButton
    private lateinit var btnYouTubeLogin: MaterialButton
    private lateinit var btnContinue: MaterialButton
    private lateinit var tvSpotifyStatus: TextView
    private lateinit var tvYouTubeStatus: TextView
    private lateinit var progressSpotify: CircularProgressIndicator
    private lateinit var progressYouTube: CircularProgressIndicator

    private lateinit var spotifyAuthManager: SpotifyAuthManager
    private lateinit var authService: AuthorizationService
    private var authState: AuthState? = null

    companion object {
        const val RC_AUTH = 100
        private const val PREFS_NAME = "SpotifyAuthPrefs"
        private const val KEY_AUTH_STATE = "auth_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set a color from your gradient as the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_dark)


        // Initialize AuthorizationService
        authService = AuthorizationService(this)
        spotifyAuthManager = SpotifyAuthManager(this)

        initializeViews()
        setupClickListeners()
        restoreAuthState()
        checkAuthStatus()
    }

    private fun initializeViews() {
        btnSpotifyLogin = findViewById(R.id.btnSpotifyLogin)
        btnYouTubeLogin = findViewById(R.id.btnYouTubeLogin)
        btnContinue = findViewById(R.id.btnContinue)
        tvSpotifyStatus = findViewById(R.id.tvSpotifyStatus)
        tvYouTubeStatus = findViewById(R.id.tvYouTubeStatus)
        progressSpotify = findViewById(R.id.progressSpotify)
        progressYouTube = findViewById(R.id.progressYouTube)

        // Initially disable continue button and hide progress indicators
        btnContinue.isEnabled = false
        progressSpotify.visibility = View.GONE
        progressYouTube.visibility = View.GONE
    }

    private fun setupClickListeners() {
        btnSpotifyLogin.setOnClickListener {
            if (authState?.isAuthorized == true) {
                // If already authorized, logout
                handleSpotifyLogout()
            } else {
                // Start auth flow
                startSpotifyAuth()
            }
        }

        btnYouTubeLogin.setOnClickListener {
            // YouTube login implementation
            updateYouTubeStatus(true)
        }

        btnContinue.setOnClickListener {
            startMainActivity()
        }
    }

    private fun startSpotifyAuth() {
        progressSpotify.visibility = View.VISIBLE
        btnSpotifyLogin.isEnabled = false
        spotifyAuthManager.startAuth()
    }

    private fun handleSpotifyLogout() {
        // Clear auth state
        authState = null
        persistAuthState()
        updateSpotifyStatus(false)
        btnSpotifyLogin.text = getString(R.string.connect_spotify)

        Snackbar.make(
            btnSpotifyLogin,
            "Disconnected from Spotify",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            progressSpotify.visibility = View.GONE
            btnSpotifyLogin.isEnabled = true

            val response = AuthorizationResponse.fromIntent(data ?: return)
            val exception = AuthorizationException.fromIntent(data)

            when {
                response != null -> {
                    handleAuthResponse(response)
                }
                exception != null -> {
                    handleAuthError(exception)
                }
                else -> {
                    handleAuthCancellation()
                }
            }
        }
    }

    private fun handleAuthResponse(response: AuthorizationResponse) {
        // Create new auth state
        authState = AuthState(response, null)

        // Exchange authorization code for token
        lifecycleScope.launch {
            try {
                progressSpotify.visibility = View.VISIBLE
                btnSpotifyLogin.isEnabled = false

                authService.performTokenRequest(
                    response.createTokenExchangeRequest()
                ) { tokenResponse, exception ->
                    when {
                        tokenResponse != null -> {
                            authState?.update(tokenResponse, exception)
                            persistAuthState()
                            runOnUiThread {
                                updateSpotifyStatus(true)
                                btnSpotifyLogin.text = getString(R.string.disconnect_spotify)
                            }
                        }
                        exception != null -> {
                            handleAuthError(exception)
                        }
                    }
                    runOnUiThread {
                        progressSpotify.visibility = View.GONE
                        btnSpotifyLogin.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    private fun handleAuthError(exception: Exception) {
        Snackbar.make(
            btnSpotifyLogin,
            "Authentication failed: ${exception.message}",
            Snackbar.LENGTH_LONG
        ).show()
        updateSpotifyStatus(false)
    }

    private fun handleAuthCancellation() {
        Snackbar.make(
            btnSpotifyLogin,
            "Authentication cancelled",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun updateSpotifyStatus(connected: Boolean) {
        tvSpotifyStatus.text = if (connected) "✓ Spotify Connected" else "× Spotify Not Connected"
        tvSpotifyStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (connected) R.color.spotify_green else R.color.black
            )
        )
        updateContinueButton()
    }

    private fun updateYouTubeStatus(connected: Boolean) {
        tvYouTubeStatus.text = if (connected) "✓ YouTube Connected" else "× YouTube Not Connected"
        tvYouTubeStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (connected) R.color.youtube_red else R.color.black
            )
        )
        updateContinueButton()
    }

    private fun updateContinueButton() {
        val spotifyConnected = tvSpotifyStatus.text.startsWith("✓")
        val youtubeConnected = tvYouTubeStatus.text.startsWith("✓")
        btnContinue.isEnabled = spotifyConnected && youtubeConnected
    }

    private fun persistAuthState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_AUTH_STATE, authState?.jsonSerializeString())
            apply()
        }
    }

    private fun restoreAuthState() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_AUTH_STATE, null)
        if (jsonString != null) {
            try {
                authState = AuthState.jsonDeserialize(jsonString)
            } catch (e: JSONException) {
                // Handle deserialization error
            }
        }
    }

    private fun checkAuthStatus() {
        if (authState?.isAuthorized == true) {
            updateSpotifyStatus(true)
            btnSpotifyLogin.text = getString(R.string.disconnect_spotify)
        } else {
            updateSpotifyStatus(false)
            btnSpotifyLogin.text = getString(R.string.connect_spotify)
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}