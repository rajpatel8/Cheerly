package com.rajkumar.cheerly.TabLayout.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.rajkumar.cheerly.Activity.ActivityAdapter
import com.rajkumar.cheerly.Activity.ActivityRepository
import com.rajkumar.cheerly.Activity.Models.ActivityLocation
import com.rajkumar.cheerly.MoodRecommendationActivity
import com.rajkumar.cheerly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ActivitiesFragment : Fragment() {
    private lateinit var activityRecyclerView: RecyclerView
    private lateinit var locationProgressBar: ProgressBar
    private lateinit var locationStatusText: TextView
    private var selectedMood: String = "Happy"
    private lateinit var activityRepository: ActivityRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        activityRecyclerView = view.findViewById(R.id.activityRecyclerView)
        locationProgressBar = view.findViewById(R.id.locationProgressBar)
        locationStatusText = view.findViewById(R.id.locationStatusText)

        // Initialize repository and location client
        activityRepository = ActivityRepository.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Setup RecyclerView
        activityRecyclerView.layoutManager = LinearLayoutManager(context)

        // Get mood from activity
        selectedMood = (activity as? MoodRecommendationActivity)?.getSelectedMood() ?: "Happy"

        // Check location permission and load activities
        checkLocationPermissionAndLoad()
    }

    private fun checkLocationPermissionAndLoad() {
        when {
            checkLocationPermission() -> {
                loadActivities()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show explanation why we need location
                locationStatusText.text = "Location permission is needed for nearby activities"
                requestLocationPermission()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadActivities()
                } else {
                    locationStatusText.text = "Location permission denied. Cannot show nearby activities."
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadActivities() {
        if (!checkLocationPermission()) {
            locationStatusText.text = "Location permission needed for nearby activities"
            return
        }

        locationProgressBar.visibility = View.VISIBLE
        locationStatusText.text = "Finding nearby activities..."

        // Using viewLifecycleOwner.lifecycleScope instead of just lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get location in IO context
                val location = withContext(Dispatchers.IO) {
                    try {
                        fusedLocationClient.lastLocation.await()
                    } catch (e: SecurityException) {
                        null // Handle permission denial
                    } catch (e: Exception) {
                        null // Handle other exceptions
                    }
                }

                // Process location and get activities
                location?.let {
                    val activityLocation = ActivityLocation(it.latitude, it.longitude)
                    // Get activities in IO context
                    val activities = withContext(Dispatchers.IO) {
                        activityRepository.getActivityRecommendations(
                            selectedMood,
                            activityLocation,
                            it.accuracy
                        )
                    }

                    // Update UI in Main context
                    withContext(Dispatchers.Main) {
                        if (activities.isNotEmpty()) {
                            activityRecyclerView.adapter = ActivityAdapter(activities)
                            locationStatusText.text = "Found nearby activities"
                        } else {
                            locationStatusText.text = "No activities found nearby"
                        }
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        locationStatusText.text = "Couldn't get location. Please try again."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    locationStatusText.text = "Error loading activities: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    locationProgressBar.visibility = View.GONE
                }
            }
        }
    }

    fun refreshContent(mood: String) {
        selectedMood = mood
        if (checkLocationPermission()) {
            loadActivities()
        }
    }
}