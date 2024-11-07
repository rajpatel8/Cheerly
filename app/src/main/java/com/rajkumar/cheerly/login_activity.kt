package com.rajkumar.cheerly

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
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
    private lateinit var googleSignInClient: GoogleSignInClient
    private var authState: AuthState? = null

    companion object {
        const val RC_AUTH = 100
        const val RC_GOOGLE_SIGN_IN = 101
        private const val PREFS_NAME = "SpotifyAuthPrefs"
        private const val KEY_AUTH_STATE = "auth_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_dark)

        // Initialize services and managers
        authService = AuthorizationService(this)
        spotifyAuthManager = SpotifyAuthManager(this)
        setupGoogleSignIn()

        initializeViews()
        setupClickListeners()
        restoreAuthState()
        checkAuthStatus()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope("https://www.googleapis.com/auth/youtube.readonly"))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
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
                handleSpotifyLogout()
            } else {
                startSpotifyAuth()
            }
        }

        btnYouTubeLogin.setOnClickListener {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                handleYouTubeLogout()
            } else {
                startYouTubeSignIn()
            }
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

    private fun startYouTubeSignIn() {
        progressYouTube.visibility = View.VISIBLE
        btnYouTubeLogin.isEnabled = false
        startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun handleSpotifyLogout() {
        authState = null
        persistAuthState()
        updateSpotifyStatus(false)
        btnSpotifyLogin.text = getString(R.string.connect_spotify)
        showSuccess("Disconnected from Spotify")
    }

    private fun handleYouTubeLogout() {
        progressYouTube.visibility = View.VISIBLE
        btnYouTubeLogin.isEnabled = false

        googleSignInClient.signOut().addOnCompleteListener {
            progressYouTube.visibility = View.GONE
            btnYouTubeLogin.isEnabled = true
            updateYouTubeStatus(false)
            btnYouTubeLogin.text = getString(R.string.connect_youtube)
            showSuccess("Disconnected from YouTube")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_AUTH -> {
                progressSpotify.visibility = View.GONE
                btnSpotifyLogin.isEnabled = true

                val response = AuthorizationResponse.fromIntent(data ?: return)
                val exception = AuthorizationException.fromIntent(data)

                when {
                    response != null -> handleAuthResponse(response)
                    exception != null -> handleAuthError(exception)
                    else -> handleAuthCancellation()
                }
            }
            RC_GOOGLE_SIGN_IN -> handleGoogleResult(data)
        }
    }

    private fun handleAuthResponse(response: AuthorizationResponse) {
        authState = AuthState(response, null)

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
                                btnSpotifyLogin.text = getString(R.string.disconnect)
                                showSuccess("Successfully connected to Spotify!")
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

    private fun handleGoogleResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            account?.let { googleAccount ->
                // Save Google credentials
                val sharedPrefs = getSharedPreferences("GooglePrefs", MODE_PRIVATE)
                sharedPrefs.edit().apply {
                    putString("google_id", googleAccount.id)
                    putString("google_email", googleAccount.email)
                    putString("google_display_name", googleAccount.displayName)
                    putString("google_id_token", googleAccount.idToken)
                    apply()
                }

                updateYouTubeStatus(true)
                btnYouTubeLogin.text = getString(R.string.disconnect)
                showSuccess("Successfully connected to YouTube!")
            } ?: run {
                showError("Failed to get Google account")
                updateYouTubeStatus(false)
            }
        } catch (e: ApiException) {
            showError("YouTube connection failed: ${e.message}")
            updateYouTubeStatus(false)
        } finally {
            progressYouTube.visibility = View.GONE
            btnYouTubeLogin.isEnabled = true
        }
    }

    private fun handleAuthError(exception: Exception) {
        showError("Authentication failed: ${exception.message}")
        updateSpotifyStatus(false)
    }

    private fun handleAuthCancellation() {
        showError("Authentication cancelled")
        updateSpotifyStatus(false)
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
        // Check Spotify status
        if (authState?.isAuthorized == true) {
            updateSpotifyStatus(true)
            btnSpotifyLogin.text = getString(R.string.disconnect)
        } else {
            updateSpotifyStatus(false)
            btnSpotifyLogin.text = getString(R.string.connect_spotify)
        }

        // Check YouTube status
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (googleAccount != null) {
            updateYouTubeStatus(true)
            btnYouTubeLogin.text = getString(R.string.disconnect)
        } else {
            updateYouTubeStatus(false)
            btnYouTubeLogin.text = getString(R.string.connect_youtube)
        }
    }

    @SuppressLint("NewApi")
    private fun showError(message: String) {
        Snackbar.make(btnSpotifyLogin, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.youtube_red))
            .setTextColor(getColor(android.R.color.white))
            .show()
    }

    @SuppressLint("NewApi")
    private fun showSuccess(message: String) {
        Snackbar.make(btnSpotifyLogin, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.green))
            .setTextColor(getColor(android.R.color.white))
            .show()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}