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

    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var musicSectionTitle: TextView
    private lateinit var videoSectionTitle: TextView

    private val spotifyRepository = SpotifyRepository.getInstance()
    private val youtubeRepository = YouTubeRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        // Initialize views
        musicRecyclerView = findViewById(R.id.musicRecyclerView)
        videoRecyclerView = findViewById(R.id.videoRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)
        musicSectionTitle = findViewById(R.id.musicSectionTitle)
        videoSectionTitle = findViewById(R.id.videoSectionTitle)

        // Initially hide video section
        videoSectionTitle.visibility = View.GONE
        videoRecyclerView.visibility = View.GONE

        // Get selected mood from intent
        val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"

        // Set title text
        titleText.text = "Recommendations for ${selectedMood.lowercase()} mood"

        // Setup RecyclerViews
        musicRecyclerView.layoutManager = LinearLayoutManager(this)
        videoRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load recommendations
        loadRecommendations(selectedMood)
    }

    private fun loadRecommendations(mood: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // First load music recommendations
                val tracks = spotifyRepository.getRecommendations(mood)
                musicRecyclerView.adapter = SongAdapter(tracks)

                // After music is loaded, show and load videos
                videoSectionTitle.visibility = View.VISIBLE
                videoRecyclerView.visibility = View.VISIBLE

                // Load video recommendations
                val videos = youtubeRepository.getVideoRecommendations(mood)
                videoRecyclerView.adapter = VideoAdapter(videos)

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