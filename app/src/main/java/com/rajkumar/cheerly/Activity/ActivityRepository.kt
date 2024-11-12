package com.rajkumar.cheerly.Activity

import android.util.Log
import com.rajkumar.cheerly.config.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import kotlin.math.*

class ActivityRepository {
    private val ticketmasterService: TicketmasterService
    private val weatherService: OpenWeatherService

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        ticketmasterService = Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TicketmasterService::class.java)

        weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherService::class.java)
    }

    suspend fun getActivityRecommendations(
        mood: String,
        location: Location
    ): List<NearbyActivity> = withContext(Dispatchers.IO) {
        try {
            Log.d("ActivityRepository", "Getting recommendations for $mood at $location")

            // Get weather information
            val weather = getWeatherInfo(location)
            Log.d("ActivityRepository", "Weather info: $weather")

            // Get events from Ticketmaster
            val events = getEvents(location, mood)
            Log.d("ActivityRepository", "Found ${events.size} events")

            // Create activities list combining all sources
            val activities = mutableListOf<NearbyActivity>()

            // Add events as activities
            activities.addAll(events.mapNotNull { event ->
                val venue = event._embedded?.venues?.firstOrNull()
                val location = venue?.location

                if (location != null) {
                    try {
                        NearbyActivity(
                            id = event.id,
                            name = event.name,
                            type = "event",
                            category = getMoodCategory(mood),
                            distance = calculateDistance(
                                lat1 = location.latitude,
                                lon1 = location.longitude,
                                lat2 = location.latitude,
                                lon2 = location.longitude
                            ),
                            address = buildAddress(venue),
                            imageUrl = event.images.firstOrNull()?.url,
                            openNow = true,
                            weather = weather,
                            externalLink = event.url
                        )
                    } catch (e: Exception) {
                        Log.e("ActivityRepository", "Error mapping event to activity", e)
                        null
                    }
                } else null
            })

            // Return filtered and sorted list
            activities.sortedBy { it.distance }.take(3)

        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error getting recommendations", e)
            emptyList()
        }
    }
    private fun buildAddress(venue: Venue): String {
        return listOfNotNull(
            venue.name,
            venue.address?.line1,
            venue.address?.city,
            venue.address?.state,
            venue.address?.postalCode
        ).filter { it.isNotBlank() }
            .joinToString(", ")
    }

    private suspend fun getWeatherInfo(location: Location): WeatherInfo? {
        return try {
            val response = weatherService.getWeather(
                location.latitude,
                location.longitude,
                ApiKeys.OPENWEATHER_API_KEY,
                "metric"
            )

            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    val weather = weatherResponse.weather.firstOrNull()
                    WeatherInfo(
                        temperature = weatherResponse.main.temp,
                        description = weather?.description ?: "",
                        icon = weather?.icon ?: "",
                        isGoodForActivity = isWeatherGoodForActivity(weather?.description ?: "")
                    )
                }
            } else null
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error getting weather", e)
            null
        }
    }

    private suspend fun getEvents(location: Location, mood: String): List<Event> {
        return try {
            val response = ticketmasterService.searchEvents(
                apiKey = ApiKeys.TICKETMASTER_API_KEY,
                latLong = "${location.latitude},${location.longitude}",
                radius = "30", // 30 mile radius
                size = 10
            )

            if (response.isSuccessful) {
                response.body()?._embedded?.events?.filter { event ->
                    // Filter out events without proper location data
                    event._embedded?.venues?.firstOrNull()?.location != null
                } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error getting events", e)
            emptyList()
        }
    }

    private fun isWeatherGoodForActivity(description: String): Boolean {
        val goodConditions = listOf(
            "clear",
            "sunny",
            "few clouds",
            "scattered clouds",
            "partly cloudy"
        )
        return goodConditions.any { description.lowercase().contains(it) }
    }

    private fun getMoodCategory(mood: String): String {
        return when (mood.lowercase()) {
            "happy" -> "Entertainment"
            "sad" -> "Relaxation"
            "excited" -> "Active Events"
            "relaxed" -> "Peaceful Places"
            else -> "General Activities"
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers

        val lat1Rad = lat1.toRadians()
        val lat2Rad = lat2.toRadians()
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()

        val a = sin(dLat/2) * sin(dLat/2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon/2) * sin(dLon/2)

        val c = 2 * atan2(sqrt(a), sqrt(1-a))

        return round(R * c * 10) / 10 // Round to 1 decimal place
    }

    private fun Double.toRadians(): Double = this * PI / 180.0

    companion object {
        @Volatile
        private var instance: ActivityRepository? = null

        fun getInstance(): ActivityRepository {
            return instance ?: synchronized(this) {
                instance ?: ActivityRepository().also { instance = it }
            }
        }
    }
}