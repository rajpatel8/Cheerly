package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class LoginActivity : ComponentActivity() {
    private lateinit var btnSpotifyLogin: MaterialButton
    private lateinit var btnYouTubeLogin: MaterialButton
    private lateinit var btnContinue: MaterialButton
    private lateinit var tvSpotifyStatus: TextView
    private lateinit var tvYouTubeStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnSpotifyLogin = findViewById(R.id.btnSpotifyLogin)
        btnYouTubeLogin = findViewById(R.id.btnYouTubeLogin)
        btnContinue = findViewById(R.id.btnContinue)
        tvSpotifyStatus = findViewById(R.id.tvSpotifyStatus)
        tvYouTubeStatus = findViewById(R.id.tvYouTubeStatus)

        // Initially disable continue button
        btnContinue.isEnabled = false
    }

    private fun setupClickListeners() {
        btnSpotifyLogin.setOnClickListener {
            // For testing UI updates
            updateSpotifyStatus(true)
            Snackbar.make(it, "Spotify login clicked", Snackbar.LENGTH_SHORT).show()
        }

        btnYouTubeLogin.setOnClickListener {
            // For testing UI updates
            updateYouTubeStatus(true)
            Snackbar.make(it, "YouTube login clicked", Snackbar.LENGTH_SHORT).show()
        }

        btnContinue.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun updateSpotifyStatus(connected: Boolean) {
        tvSpotifyStatus.text = if (connected) "✓ Spotify Connected" else "× Spotify Not Connected"
        tvSpotifyStatus.setTextColor(
            ContextCompat.getColor(this, if (connected) R.color.spotify_green else R.color.black)
        )
        updateContinueButton()
    }

    private fun updateYouTubeStatus(connected: Boolean) {
        tvYouTubeStatus.text = if (connected) "✓ YouTube Connected" else "× YouTube Not Connected"
        tvYouTubeStatus.setTextColor(
            ContextCompat.getColor(this, if (connected) R.color.youtube_red else R.color.black)
        )
        updateContinueButton()
    }

    private fun updateContinueButton() {
        val spotifyConnected = tvSpotifyStatus.text.startsWith("✓")
        val youtubeConnected = tvYouTubeStatus.text.startsWith("✓")
        btnContinue.isEnabled = spotifyConnected && youtubeConnected
    }
}