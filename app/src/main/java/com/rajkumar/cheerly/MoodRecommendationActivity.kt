package com.rajkumar.cheerly

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.rajkumar.cheerly.Music.SongAdapter
import com.rajkumar.cheerly.Music.SpotifyRepository
import com.rajkumar.cheerly.Podcast.PodcastAdapter
import com.rajkumar.cheerly.Podcast.PodcastRepository
import com.rajkumar.cheerly.Video.VideoAdapter
import com.rajkumar.cheerly.Video.YouTubeRepository
import com.rajkumar.cheerly.Activity.ActivityAdapter
import com.rajkumar.cheerly.Activity.ActivityRepository
import kotlinx.coroutines.launch

class MoodRecommendationActivity : ComponentActivity() {

    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var podcastRecyclerView: RecyclerView
    private lateinit var activityRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var musicSectionTitle: TextView
    private lateinit var videoSectionTitle: TextView
    private lateinit var podcastSectionTitle: TextView
    private lateinit var activitySectionTitle: TextView

    private lateinit var spotifyRepository: SpotifyRepository
    private val youtubeRepository = YouTubeRepository.getInstance()
    private val podcastRepository = PodcastRepository.getInstance()
    private val activityRepository = ActivityRepository.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        // Check location permission before loading
        if (checkLocationPermission()) {
            loadRecommendations(selectedMood)
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadRecommendations(selectedMood)
                } else {
                    // Load recommendations without activities if location permission denied
                    loadRecommendationsWithoutLocation(selectedMood)
                }
            }
        }
    }

    private fun initializeViews() {
        musicRecyclerView = findViewById(R.id.musicRecyclerView)
        videoRecyclerView = findViewById(R.id.videoRecyclerView)
        podcastRecyclerView = findViewById(R.id.podcastRecyclerView)
        activityRecyclerView = findViewById(R.id.activityRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)
        musicSectionTitle = findViewById(R.id.musicSectionTitle)
        videoSectionTitle = findViewById(R.id.videoSectionTitle)
        podcastSectionTitle = findViewById(R.id.podcastSectionTitle)
        activitySectionTitle = findViewById(R.id.activitySectionTitle)

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
        activitySectionTitle.visibility = View.GONE
        activityRecyclerView.visibility = View.GONE
    }

    private fun setupRecyclerViews() {
        musicRecyclerView.layoutManager = LinearLayoutManager(this)
        videoRecyclerView.layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = LinearLayoutManager(this)
        activityRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadRecommendations(mood: String) {
        progressBar.visibility = View.VISIBLE
        setInitialVisibility()

        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    loadRecommendationsWithLocation(mood, location)
                }
                .addOnFailureListener {
                    loadRecommendationsWithoutLocation(mood)
                }
        } else {
            loadRecommendationsWithoutLocation(mood)
        }
    }

    private fun loadRecommendationsWithLocation(mood: String, location: Location?) {
        lifecycleScope.launch {
            try {
                var musicLoaded = false
                var videosLoaded = false
                var podcastsLoaded = false
                var activitiesLoaded = false

                // Load music recommendations
                try {
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

                // Load video recommendations
                try {
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

                // Load podcast recommendations
                try {
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

                // Load activity recommendations if location is available
                if (location != null) {
                    try {
                        val activityLocation = com.rajkumar.cheerly.Activity.Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        val activities = activityRepository.getActivityRecommendations(mood, activityLocation)
                        if (activities.isNotEmpty()) {
                            activityRecyclerView.adapter = ActivityAdapter(activities)
                            activitySectionTitle.visibility = View.VISIBLE
                            activityRecyclerView.visibility = View.VISIBLE
                            activitiesLoaded = true
                        }
                    } catch (e: Exception) {
                        Log.e("MoodRecommendation", "Error loading activities", e)
                    }
                }

                // Check if at least one type of content was loaded
                if (!musicLoaded && !videosLoaded && !podcastsLoaded && !activitiesLoaded) {
                    showError("No recommendations found for your mood")
                }

            } catch (e: Exception) {
                Log.e("MoodRecommendation", "Error in recommendation loading", e)
                showError("Error loading recommendations: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadRecommendationsWithoutLocation(mood: String) {
        // Same as loadRecommendationsWithLocation but without activity loading
        lifecycleScope.launch {
            try {
                var musicLoaded = false
                var videosLoaded = false
                var podcastsLoaded = false

                // Load music recommendations
                try {
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

                // Load video recommendations
                try {
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

                // Load podcast recommendations
                try {
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
