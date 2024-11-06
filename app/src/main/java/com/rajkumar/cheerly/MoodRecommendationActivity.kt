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

    private val spotifyRepository = SpotifyRepository.getInstance()
    private val youtubeRepository = YouTubeRepository.getInstance()
    private val podcastRepository = PodcastRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        // Initialize views
        musicRecyclerView = findViewById(R.id.musicRecyclerView)
        videoRecyclerView = findViewById(R.id.videoRecyclerView)
        podcastRecyclerView = findViewById(R.id.podcastRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)
        musicSectionTitle = findViewById(R.id.musicSectionTitle)
        videoSectionTitle = findViewById(R.id.videoSectionTitle)
        podcastSectionTitle = findViewById(R.id.podcastSectionTitle)

        // Initially hide video and podcast sections
        videoSectionTitle.visibility = View.GONE
        videoRecyclerView.visibility = View.GONE
        podcastSectionTitle.visibility = View.GONE
        podcastRecyclerView.visibility = View.GONE

        // Get selected mood from intent
        val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"

        // Set title text
        titleText.text = "Recommendations for ${selectedMood.lowercase()} mood"

        // Setup RecyclerViews with layoutManagers
        musicRecyclerView.layoutManager = LinearLayoutManager(this)
        videoRecyclerView.layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load recommendations
        loadRecommendations(selectedMood)
    }

    private fun loadRecommendations(mood: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // First load music recommendations
                val tracks = spotifyRepository.getRecommendations(mood)
                if (tracks.isNotEmpty()) {
                    musicRecyclerView.adapter = SongAdapter(tracks)
                    musicSectionTitle.visibility = View.VISIBLE
                    musicRecyclerView.visibility = View.VISIBLE
                } else {
                    musicSectionTitle.visibility = View.GONE
                    musicRecyclerView.visibility = View.GONE
                }

                // Load video recommendations
                val videos = youtubeRepository.getVideoRecommendations(mood)
                if (videos.isNotEmpty()) {
                    videoRecyclerView.adapter = VideoAdapter(videos)
                    videoSectionTitle.visibility = View.VISIBLE
                    videoRecyclerView.visibility = View.VISIBLE
                } else {
                    videoSectionTitle.visibility = View.GONE
                    videoRecyclerView.visibility = View.GONE
                }

                // Load podcast recommendations
                val podcasts = podcastRepository.getPodcastRecommendations(mood)
                if (podcasts.isNotEmpty()) {
                    podcastRecyclerView.adapter = PodcastAdapter(podcasts)
                    podcastSectionTitle.visibility = View.VISIBLE
                    podcastRecyclerView.visibility = View.VISIBLE
                } else {
                    podcastSectionTitle.visibility = View.GONE
                    podcastRecyclerView.visibility = View.GONE
                }

                // Log for debugging
                Log.d("MoodRecommendation", """
                    Content Loaded:
                    Music Tracks: ${tracks.size}
                    Videos: ${videos.size}
                    Podcasts: ${podcasts.size}
                """.trimIndent())

            } catch (e: Exception) {
                Toast.makeText(
                    this@MoodRecommendationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("MoodRecommendation", "Error loading recommendations", e)
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup if needed
    }
}