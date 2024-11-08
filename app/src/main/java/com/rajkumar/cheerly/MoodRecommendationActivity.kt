package com.rajkumar.cheerly

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rajkumar.cheerly.Music.SongAdapter
import com.rajkumar.cheerly.Music.SpotifyRepository
import com.rajkumar.cheerly.Podcast.PodcastAdapter
import com.rajkumar.cheerly.Podcast.PodcastRepository
import com.rajkumar.cheerly.Video.VideoAdapter
import com.rajkumar.cheerly.Video.YouTubeRepository
import kotlinx.coroutines.launch

class MoodRecommendationActivity : ComponentActivity() {

    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var podcastRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var musicSectionTitle: TextView
    private lateinit var videoSectionTitle: TextView
    private lateinit var podcastSectionTitle: TextView

    private lateinit var spotifyRepository: SpotifyRepository
    private val youtubeRepository = YouTubeRepository.getInstance()
    private val podcastRepository = PodcastRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        // Initialize repositories
        spotifyRepository = SpotifyRepository.getInstance(this)

        // Initialize views
        initializeViews()

        // Get selected mood from intent
        val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"

        // Set title text
        titleText.text = "Recommendations for ${selectedMood.lowercase()} mood"

        // Setup RecyclerViews
        setupRecyclerViews()

        // Load recommendations
        loadRecommendations(selectedMood)
    }

    private fun initializeViews() {
        musicRecyclerView = findViewById(R.id.musicRecyclerView)
        videoRecyclerView = findViewById(R.id.videoRecyclerView)
        podcastRecyclerView = findViewById(R.id.podcastRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)
        musicSectionTitle = findViewById(R.id.musicSectionTitle)
        videoSectionTitle = findViewById(R.id.videoSectionTitle)
        podcastSectionTitle = findViewById(R.id.podcastSectionTitle)

        // Initially hide sections
        setInitialVisibility()
    }

    private fun setInitialVisibility() {
        videoSectionTitle.visibility = View.GONE
        videoRecyclerView.visibility = View.GONE
        podcastSectionTitle.visibility = View.GONE
        podcastRecyclerView.visibility = View.GONE
        musicSectionTitle.visibility = View.GONE
        musicRecyclerView.visibility = View.GONE
    }

    private fun setupRecyclerViews() {
        musicRecyclerView.layoutManager = LinearLayoutManager(this)
        videoRecyclerView.layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadRecommendations(mood: String) {
        progressBar.visibility = View.VISIBLE
        setInitialVisibility()

        lifecycleScope.launch {
            try {
                // Load all recommendations in parallel
                var musicLoaded = false
                var videosLoaded = false
                var podcastsLoaded = false

                try {
                    // Music recommendations
                    val tracks = spotifyRepository.getRecommendations(mood)
                    if (tracks.isNotEmpty()) {
                        musicRecyclerView.adapter = SongAdapter(tracks)
                        musicSectionTitle.visibility = View.VISIBLE
                        musicRecyclerView.visibility = View.VISIBLE
                        musicLoaded = true
                    }
                } catch (e: Exception) {
                    Log.e("MoodRecommendation", "Error loading music", e)
                }

                try {
                    // Video recommendations
                    val videos = youtubeRepository.getVideoRecommendations(mood)
                    if (videos.isNotEmpty()) {
                        videoRecyclerView.adapter = VideoAdapter(videos)
                        videoSectionTitle.visibility = View.VISIBLE
                        videoRecyclerView.visibility = View.VISIBLE
                        videosLoaded = true
                    }
                } catch (e: Exception) {
                    Log.e("MoodRecommendation", "Error loading videos", e)
                }

                try {
                    // Podcast recommendations
                    val podcasts = podcastRepository.getPodcastRecommendations(mood)
                    if (podcasts.isNotEmpty()) {
                        podcastRecyclerView.adapter = PodcastAdapter(podcasts)
                        podcastSectionTitle.visibility = View.VISIBLE
                        podcastRecyclerView.visibility = View.VISIBLE
                        podcastsLoaded = true
                    }
                } catch (e: Exception) {
                    Log.e("MoodRecommendation", "Error loading podcasts", e)
                }

                // Check if at least one type of content was loaded
                if (!musicLoaded && !videosLoaded && !podcastsLoaded) {
                    showError("No recommendations found for your mood")
                } else {
                    Log.d("MoodRecommendation", """
                        Content Loading Status:
                        Music: $musicLoaded
                        Videos: $videosLoaded
                        Podcasts: $podcastsLoaded
                    """.trimIndent())
                }

            } catch (e: Exception) {
                Log.e("MoodRecommendation", "Error in recommendation loading", e)
                showError("Error loading recommendations: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }
}