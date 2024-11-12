package com.rajkumar.cheerly

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
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
import com.google.android.gms.location.*
import com.rajkumar.cheerly.Music.SongAdapter
import com.rajkumar.cheerly.Music.SpotifyRepository
import com.rajkumar.cheerly.Podcast.PodcastAdapter
import com.rajkumar.cheerly.Podcast.PodcastRepository
import com.rajkumar.cheerly.Video.VideoAdapter
import com.rajkumar.cheerly.Video.VideoRepository
import com.rajkumar.cheerly.Activity.ActivityAdapter
import com.rajkumar.cheerly.Activity.ActivityRepository
import com.rajkumar.cheerly.Activity.Models.ActivityLocation
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
    private lateinit var locationProgressBar: ProgressBar
    private lateinit var locationStatusText: TextView

    // Initialize repositories
    private lateinit var spotifyRepository: SpotifyRepository
    private lateinit var youtubeRepository: VideoRepository
    private lateinit var podcastRepository: PodcastRepository
    private lateinit var activityRepository: ActivityRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var locationUpdateCount = 0
    private val MAX_LOCATION_UPDATES = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_dark)

        // Initialize repositories with context
        spotifyRepository = SpotifyRepository.getInstance(this)
        youtubeRepository = VideoRepository.getInstance(this)
        podcastRepository = PodcastRepository.getInstance()
        activityRepository = ActivityRepository.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize views and setup
        initializeViews()
        setupRecyclerViews()
        setupLocation()

        // Get selected mood and set title
        val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"
        titleText.text = "Recommendations for ${selectedMood.lowercase()} mood"

        // Start loading non-location content immediately
        loadNonLocationContent(selectedMood)

        // Handle location-based content separately
        if (checkLocationPermission()) {
            locationStatusText.text = "Finding nearby activities..."
            locationProgressBar.visibility = View.VISIBLE
            startLocationUpdates()
        } else {
            locationStatusText.text = "Location permission required for nearby activities"
            requestLocationPermission()
        }
    }

    private fun setupLocation() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMinUpdateDistanceMeters(20f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    locationUpdateCount++
                    val selectedMood = intent.getStringExtra("selectedMood") ?: "Happy"

                    when {
                        location.accuracy <= 50f -> {
                            loadActivities(selectedMood, location)
                            stopLocationUpdates()
                            locationStatusText.text = "Found nearby activities"
                            locationProgressBar.visibility = View.GONE
                        }
                        locationUpdateCount >= MAX_LOCATION_UPDATES -> {
                            loadActivities(selectedMood, location)
                            stopLocationUpdates()
                            locationStatusText.text = "Using approximate location"
                            locationProgressBar.visibility = View.GONE
                        }
                        else -> {
                            locationStatusText.text = "Improving location accuracy..."
                        }
                    }
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
        locationProgressBar = findViewById(R.id.locationProgressBar)
        locationStatusText = findViewById(R.id.locationStatusText)

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
        locationProgressBar.visibility = View.GONE
    }

    private fun setupRecyclerViews() {
        musicRecyclerView.layoutManager = LinearLayoutManager(this)
        videoRecyclerView.layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = LinearLayoutManager(this)
        activityRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadNonLocationContent(mood: String) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            try {
                // Load music recommendations
                try {
                    val tracks = spotifyRepository.getRecommendations(mood)
                    if (tracks.isNotEmpty()) {
                        musicRecyclerView.adapter = SongAdapter(tracks)
                        musicSectionTitle.visibility = View.VISIBLE
                        musicRecyclerView.visibility = View.VISIBLE
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
                    }
                } catch (e: Exception) {
                    Log.e("MoodRecommendation", "Error loading podcasts", e)
                }

            } catch (e: Exception) {
                Log.e("MoodRecommendation", "Error loading content", e)
                showError("Error loading recommendations: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadActivities(mood: String, androidLocation: android.location.Location) {
        lifecycleScope.launch {
            try {
                val activityLocation = ActivityLocation(
                    latitude = androidLocation.latitude,
                    longitude = androidLocation.longitude
                )

                val activities = activityRepository.getActivityRecommendations(
                    mood = mood,
                    location = activityLocation,
                    accuracy = androidLocation.accuracy
                )

                if (activities.isNotEmpty()) {
                    activityRecyclerView.adapter = ActivityAdapter(activities)
                    activitySectionTitle.visibility = View.VISIBLE
                    activityRecyclerView.visibility = View.VISIBLE
                } else {
                    locationStatusText.text = "No activities found nearby"
                }
            } catch (e: Exception) {
                Log.e("MoodRecommendation", "Error loading activities", e)
                locationStatusText.text = "Error loading activities: ${e.message}"
            }
        }
    }

    private fun startLocationUpdates() {
        try {
            if (checkLocationPermission()) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } catch (e: Exception) {
            Log.e("Location", "Error starting location updates", e)
            locationStatusText.text = "Error getting location: ${e.message}"
            locationProgressBar.visibility = View.GONE
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationStatusText.text = "Finding nearby activities..."
                    locationProgressBar.visibility = View.VISIBLE
                    startLocationUpdates()
                } else {
                    locationStatusText.text = "Location permission denied"
                    locationProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}