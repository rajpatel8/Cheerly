package com.rajkumar.cheerly.Activity

import android.net.Uri
import android.util.Log
import com.rajkumar.cheerly.Activity.Models.ActivityLocation
import com.rajkumar.cheerly.Activity.Models.ActivityParameters
import com.rajkumar.cheerly.Activity.Models.Event
import com.rajkumar.cheerly.Activity.Models.EventImage
import com.rajkumar.cheerly.Activity.Models.NearbyActivity
import com.rajkumar.cheerly.Activity.Models.Venue
import com.rajkumar.cheerly.Activity.Models.WeatherInfo
import com.rajkumar.cheerly.config.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.*

class ActivityRepository private constructor() {
    private val ticketmasterService: TicketmasterService
    private val weatherService: OpenWeatherService

    private val moodParameters = mapOf(
        "happy" to ActivityParameters(
            categories = listOf("music", "comedy", "food", "family", "attractions"),
            keywords = listOf("festival", "carnival", "concert", "amusement", "party"),
            maxDistance = 30.0,
            preferIndoor = false,
            preferPopular = true
        ),
        "sad" to ActivityParameters(
            categories = listOf("nature", "art", "wellness", "spa", "museum"),
            keywords = listOf("peaceful", "quiet", "relaxing", "healing", "therapeutic"),
            maxDistance = 15.0,
            preferIndoor = true,
            preferPopular = false
        ),
        "excited" to ActivityParameters(
            categories = listOf("sports", "adventure", "music", "dance", "fitness"),
            keywords = listOf("high-energy", "extreme", "adventure", "competition", "active"),
            maxDistance = 40.0,
            preferIndoor = false,
            preferPopular = true
        ),
        "relaxed" to ActivityParameters(
            categories = listOf("parks", "gardens", "cafe", "library", "gallery"),
            keywords = listOf("calm", "serene", "peaceful", "quiet", "nature"),
            maxDistance = 20.0,
            preferIndoor = false,
            preferPopular = false
        ),
        "bored" to ActivityParameters(
            categories = listOf("entertainment", "gaming", "sports", "shopping", "attractions"),
            keywords = listOf("exciting", "interactive", "unique", "fun", "entertaining"),
            maxDistance = 25.0,
            preferIndoor = true,
            preferPopular = true
        ),
        "anxious" to ActivityParameters(
            categories = listOf("wellness", "nature", "yoga", "meditation", "cafe"),
            keywords = listOf("calming", "peaceful", "quiet", "therapeutic", "soothing"),
            maxDistance = 10.0,
            preferIndoor = true,
            preferPopular = false
        ),
        "focused" to ActivityParameters(
            categories = listOf("library", "cafe", "coworking", "museum", "study"),
            keywords = listOf("quiet", "productive", "studious", "learning", "concentration"),
            maxDistance = 15.0,
            preferIndoor = true,
            preferPopular = false
        )
    )

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
        location: ActivityLocation,
        accuracy: Float
    ): List<NearbyActivity> = withContext(Dispatchers.IO) {
        try {
            Log.d("ActivityRepository", "Getting recommendations for $mood at $location")

            val parameters = moodParameters[mood.lowercase()] ?: moodParameters["happy"]!!
            val searchRadius = calculateSearchRadius(accuracy, parameters.maxDistance)
            val weather = getWeatherInfo(location)
            val events = getEvents(location, searchRadius)

            processEvents(events, location, weather, parameters, searchRadius)
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error getting recommendations", e)
            emptyList()
        }
    }

    private suspend fun getEvents(location: ActivityLocation, radius: Double): List<Event> {
        return try {
            val response = ticketmasterService.searchEvents(
                apiKey = ApiKeys.TICKETMASTER_API_KEY,
                latLong = "${location.latitude},${location.longitude}",
                radius = radius.toInt().toString()
            )

            if (response.isSuccessful) {
                response.body()?._embedded?.events ?: emptyList()
            } else {
                Log.e("ActivityRepository", "Error getting events: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error fetching events", e)
            emptyList()
        }
    }

    private suspend fun getWeatherInfo(location: ActivityLocation): WeatherInfo? {
        return try {
            val response = weatherService.getWeather(
                lat = location.latitude,
                lon = location.longitude,
                apiKey = ApiKeys.OPENWEATHER_API_KEY,
                units = "metric"
            )

            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    val weather = weatherResponse.weather.firstOrNull()
                    WeatherInfo(
                        temperature = weatherResponse.main.temp,
                        description = weather?.description ?: "",
                        icon = weather?.icon ?: "",
                        isGoodForActivity = isWeatherGoodForActivity(weather?.description)
                    )
                }
            } else null
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error getting weather", e)
            null
        }
    }

    private fun processEvents(
        events: List<Event>,
        location: ActivityLocation,
        weather: WeatherInfo?,
        parameters: ActivityParameters,
        searchRadius: Double
    ): List<NearbyActivity> {
        return events.mapNotNull { event ->
            createNearbyActivity(event, location, weather, parameters)
        }.filter { activity ->
            // Filter based on distance and weather conditions
            val weatherSuitable = if (!parameters.preferIndoor) {
                activity.weather?.isGoodForActivity == true
            } else true

            weatherSuitable && activity.distance <= searchRadius
        }.sortedWith(
            compareBy<NearbyActivity> { it.distance }
                .thenByDescending { if (parameters.preferPopular) it.rating ?: 0f else 0f }
        ).take(5)
    }

    private fun createNearbyActivity(
        event: Event,
        location: ActivityLocation,
        weather: WeatherInfo?,
        parameters: ActivityParameters
    ): NearbyActivity? {
        val venue = event._embedded?.venues?.firstOrNull() ?: return null
        val venueLocation = venue.location ?: return null

        val distance = calculateDistance(
            lat1 = location.latitude,
            lon1 = location.longitude,
            lat2 = venueLocation.latitude,
            lon2 = venueLocation.longitude
        )

        val address = buildAddress(venue)

        return NearbyActivity(
            id = event.id,
            name = event.name,
            type = "event",
            category = getCategoryByMood(event, parameters),
            distance = distance,
            address = address,
            imageUrl = getBestImage(event.images),
            openNow = true, // We'll assume events are "open" if they're returned by the API
            weather = weather,
            rating = null,
            externalLink = buildMapsUrl(address),
            placeId = null
        )
    }

    private fun calculateSearchRadius(accuracy: Float, maxDistance: Double): Double {
        return when {
            accuracy <= 50f -> maxDistance
            accuracy <= 100f -> maxDistance * 0.8
            else -> maxDistance * 0.6
        }
    }

    private fun buildAddress(venue: Venue): String {
        return listOfNotNull(
            venue.name,
            venue.address?.line1,
            venue.city?.name,
            venue.state?.name
        ).joinToString(", ")
    }

    private fun buildMapsUrl(address: String): String {
        val encodedAddress = Uri.encode(address)
        return "https://www.google.com/maps/search/?api=1&query=$encodedAddress"
    }

    private fun getBestImage(images: List<EventImage>): String? {
        return images.maxByOrNull { it.width * it.height }?.url
    }

    private fun isWeatherGoodForActivity(description: String?): Boolean {
        if (description == null) return true
        val badConditions = listOf("rain", "snow", "storm", "thunder", "heavy")
        return !badConditions.any { description.lowercase().contains(it) }
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

        return round(R * c * 100) / 100 // Round to 2 decimal places
    }

    private fun Double.toRadians(): Double = this * PI / 180.0

    private fun getCategoryByMood(event: Event, parameters: ActivityParameters): String {
        return parameters.categories.firstOrNull { category ->
            event.name.lowercase().contains(category) ||
                    event.classifications?.any {
                        it.segment.name.lowercase().contains(category)
                    } == true
        } ?: "Entertainment"
    }

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