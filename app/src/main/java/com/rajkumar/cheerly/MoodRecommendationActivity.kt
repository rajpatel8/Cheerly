package com.rajkumar.cheerly

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MoodRecommendationActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private val spotifyRepository = SpotifyRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)

        // Get selected mood from intent
        val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"

        // Set title text
        titleText.text = "Music for your ${selectedMood.lowercase()} mood"

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load recommendations
        loadRecommendations(selectedMood)
    }

    private fun loadRecommendations(mood: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val tracks = spotifyRepository.getRecommendations(mood)
                recyclerView.adapter = SongAdapter(tracks)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MoodRecommendationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}