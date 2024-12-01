package com.rajkumar.cheerly.Activity

import android.util.Log
import com.rajkumar.cheerly.Activity.Models.*
import com.rajkumar.cheerly.config.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.*

class ActivityRepository private constructor() {
    private val TAG = "ActivityRepository"
    private val FOURSQUARE_API_KEY = "fsq3I0KCGb5Qn7v17dtvhhMkLm+OsKAkuAeNKmOrHlPTjG8="

    private val foursquareService: FoursquareService
    private val ticketmasterService: TicketmasterService
    private val weatherService: OpenWeatherService

    private val DEFAULT_COUNTRY = "CA"  // Default to Canada
    private val MAX_ACTUAL_DISTANCE_KM = 100.0  // Increased from 50km to 100km
    private val MIN_RADIUS_METERS = 5000.0  // Minimum 5km radius
    private val MAX_RADIUS_METERS = 50000.0  // Maximum 50km radius

    // Comprehensive mood parameters for better recommendations
    data class MoodParameters(
        val foursquareCategories: List<Int>,
        val venueKeywords: List<String>,
        val eventCategories: List<String>,
        val eventKeywords: List<String>,
        val maxDistance: Double,
        val preferIndoor: Boolean,
        val preferPopular: Boolean,
        val weatherSensitive: Boolean,
        val timeOfDayPreference: List<String>,
        val pricePreference: PricePreference,
        val moodSpecificFilters: (FoursquareVenue) -> Boolean,
    )

    private fun getMoodFromParameters(parameters: MoodParameters): String {
        // Create sets of keywords for each mood
        val moodKeywords = mapOf(
            "happy" to setOf("festival", "entertainment", "social", "party", "fun", "uplifting"),
            "sad" to setOf("peaceful", "quiet", "nature", "relaxing", "comfort", "healing"),
            "excited" to setOf("adventure", "active", "sports", "energetic", "thrilling"),
            "relaxed" to setOf("peaceful", "calm", "serene", "cozy", "tranquil"),
            "bored" to setOf("interesting", "unique", "entertaining", "novel", "experience"),
            "anxious" to setOf("quiet", "peaceful", "calming", "safe", "comfort"),
            "focused" to setOf("quiet", "study", "work", "productive", "concentration")
        )

        // Find the mood with the most keyword matches
        return moodKeywords.entries.maxByOrNull { (_, keywords) ->
            parameters.venueKeywords.count { it in keywords }
        }?.key ?: "happy"
    }

    private val moodParameters = mapOf(
        "happy" to MoodParameters(
            foursquareCategories = listOf(
                10000, // Arts & Entertainment
                13338, // Nightlife
                13003, // Park
                13065, // Plaza
                13035  // Coffee Shop
            ),
            venueKeywords = listOf("festival", "entertainment", "social", "party", "fun"),
            eventCategories = listOf("music", "comedy", "family", "attractions"),
            eventKeywords = listOf("festival", "carnival", "concert", "celebration"),
            maxDistance = 30.0,
            preferIndoor = false,
            preferPopular = true,
            weatherSensitive = true,
            timeOfDayPreference = listOf("afternoon", "evening"),
            pricePreference = PricePreference.ANY,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("entertainment") ||
                                name.contains("amusement") ||
                                name.contains("fun") ||
                                name.contains("social")
                    }
                }
            }
        ),

        "sad" to MoodParameters(
            foursquareCategories = listOf(
                13003, // Park
                13091, // Garden
                12072, // Wellness
                13035, // Coffee Shop
                12051  // Library
            ),
            venueKeywords = listOf("peaceful", "quiet", "nature", "relaxing", "comfort"),
            eventCategories = listOf("art", "wellness", "museum", "cultural"),
            eventKeywords = listOf("meditation", "exhibition", "workshop", "gallery"),
            maxDistance = 15.0,
            preferIndoor = true,
            preferPopular = false,
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.LOW,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("garden") ||
                                name.contains("park") ||
                                name.contains("spa") ||
                                name.contains("library")
                    }
                }
            }
        ),

        "excited" to MoodParameters(
            foursquareCategories = listOf(
                13338, // Nightlife
                10000, // Arts & Entertainment
                18000, // Sports
                13384, // Fitness
                13273  // Arcade
            ),
            venueKeywords = listOf("adventure", "active", "sports", "energetic", "thrilling"),
            eventCategories = listOf("sports", "music", "dance", "adventure"),
            eventKeywords = listOf("competition", "game", "performance", "challenge"),
            maxDistance = 40.0,
            preferIndoor = false,
            preferPopular = true,
            weatherSensitive = true,
            timeOfDayPreference = listOf("afternoon", "evening"),
            pricePreference = PricePreference.ANY,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("sports") ||
                                name.contains("fitness") ||
                                name.contains("dance") ||
                                name.contains("adventure")
                    }
                }
            }
        ),

        "relaxed" to MoodParameters(
            foursquareCategories = listOf(
                13003, // Park
                13091, // Garden
                13035, // Coffee Shop
                12072, // Wellness
                12089  // Spa
            ),
            venueKeywords = listOf("peaceful", "calm", "serene", "cozy", "tranquil"),
            eventCategories = listOf("wellness", "art", "cultural", "nature"),
            eventKeywords = listOf("meditation", "garden", "relaxation", "peaceful"),
            maxDistance = 20.0,
            preferIndoor = false,
            preferPopular = false,
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.MEDIUM,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("cafe") ||
                                name.contains("garden") ||
                                name.contains("spa") ||
                                name.contains("wellness")
                    }
                }
            }
        ),

        "bored" to MoodParameters(
            foursquareCategories = listOf(
                10000, // Arts & Entertainment
                13273, // Arcade
                13003, // Park
                13338, // Nightlife
                13035  // Coffee Shop
            ),
            venueKeywords = listOf("interesting", "unique", "fun", "entertaining", "novel"),
            eventCategories = listOf("entertainment", "sports", "music", "comedy"),
            eventKeywords = listOf("event", "show", "game", "experience"),
            maxDistance = 25.0,
            preferIndoor = true,
            preferPopular = true,
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon", "evening"),
            pricePreference = PricePreference.ANY,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("entertainment") ||
                                name.contains("arcade") ||
                                name.contains("activity") ||
                                name.contains("game")
                    }
                }
            }
        ),

        "anxious" to MoodParameters(
            foursquareCategories = listOf(
                13091, // Garden
                12072, // Wellness
                12089, // Spa
                13035, // Coffee Shop
                12051  // Library
            ),
            venueKeywords = listOf("quiet", "peaceful", "calming", "safe", "comfort"),
            eventCategories = listOf("wellness", "yoga", "meditation", "art"),
            eventKeywords = listOf("relaxation", "mindfulness", "peace", "wellness"),
            maxDistance = 15.0, // Keeping it close for comfort
            preferIndoor = true,
            preferPopular = false, // Prefer less crowded places
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.MEDIUM,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("wellness") ||
                                name.contains("library") ||
                                name.contains("garden") ||
                                name.contains("bookstore")
                    }
                }
            }
        ),

        "focused" to MoodParameters(
            foursquareCategories = listOf(
                13035, // Coffee Shop
                12051, // Library
                13145, // Coworking Space
                13002, // Study Area
                12072  // Wellness
            ),
            venueKeywords = listOf("quiet", "study", "work", "productive", "concentration"),
            eventCategories = listOf("learning", "workshop", "seminar", "education"),
            eventKeywords = listOf("workshop", "study", "learning", "class"),
            maxDistance = 15.0,
            preferIndoor = true,
            preferPopular = false, // Prefer quieter places
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.LOW,
            moodSpecificFilters = { venue ->
                venue.categories.any { cat ->
                    cat.name.lowercase().let { name ->
                        name.contains("library") ||
                                name.contains("coffee") ||
                                name.contains("study") ||
                                name.contains("coworking")
                    }
                }
            }
        )
    )


    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(LoggingInterceptor())
            .build()

        // Create separate clients for each service
        val foursquareClient = okHttpClient.newBuilder()
            .addInterceptor(LoggingInterceptor())  // Add logging for debugging
            .build()


        foursquareService = Retrofit.Builder()
            .baseUrl(FoursquareService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoursquareService::class.java)

        // Keep other services as they were
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

    private fun isHighPriorityForMood(activity: NearbyActivity, parameters: MoodParameters): Boolean {
        val mood = getMoodFromParameters(parameters)
        return when (mood.lowercase()) {
            "happy" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("entertainment") ||
                            cat.contains("party") ||
                            cat.contains("festival") ||
                            cat.contains("social")
                }
            }
            "sad" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("garden") ||
                            cat.contains("park") ||
                            cat.contains("wellness") ||
                            cat.contains("spa")
                }
            }
            "excited" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("sports") ||
                            cat.contains("adventure") ||
                            cat.contains("activity") ||
                            cat.contains("fitness")
                }
            }
            "relaxed" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("cafe") ||
                            cat.contains("garden") ||
                            cat.contains("spa") ||
                            cat.contains("park")
                }
            }
            "bored" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("entertainment") ||
                            cat.contains("arcade") ||
                            cat.contains("activity") ||
                            cat.contains("game")
                }
            }
            "anxious" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("spa") ||
                            cat.contains("wellness") ||
                            cat.contains("garden") ||
                            cat.contains("meditation")
                }
            }
            "focused" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("library") ||
                            cat.contains("study") ||
                            cat.contains("workspace") ||
                            cat.contains("coworking")
                }
            }
            else -> false
        }
    }

    private fun isMediumPriorityForMood(activity: NearbyActivity, parameters: MoodParameters): Boolean {
        if (isHighPriorityForMood(activity, parameters)) return false

        val mood = getMoodFromParameters(parameters)
        return when (mood.lowercase()) {
            "happy" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("cafe") ||
                            cat.contains("restaurant") ||
                            cat.contains("plaza")
                }
            }
            "sad" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("cafe") ||
                            cat.contains("bookstore") ||
                            cat.contains("tea")
                }
            }
            "excited" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("entertainment") ||
                            cat.contains("game") ||
                            cat.contains("dance")
                }
            }
            "relaxed" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("bookstore") ||
                            cat.contains("tea") ||
                            cat.contains("coffee")
                }
            }
            "bored" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("shopping") ||
                            cat.contains("museum") ||
                            cat.contains("theater")
                }
            }
            "anxious" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("cafe") ||
                            cat.contains("library") ||
                            cat.contains("bookstore")
                }
            }
            "focused" -> {
                activity.category.lowercase().let { cat ->
                    cat.contains("cafe") ||
                            cat.contains("coffee") ||
                            cat.contains("bookstore")
                }
            }
            else -> false
        }
    }

    private class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()

            val response = chain.proceed(request)

            return response
        }
    }

    suspend fun getActivityRecommendations(
        mood: String,
        location: ActivityLocation,
        accuracy: Float
    ): List<NearbyActivity> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting activity recommendations for mood: $mood")
            val parameters = moodParameters[mood.lowercase()] ?: moodParameters["happy"]!!
            val radius = calculateSearchRadius(accuracy, parameters.maxDistance)

            // Get weather info first as it affects other recommendations
            val weather = getWeatherInfo(location)

            // Fetch venues and events in parallel
            val venues = getFoursquareVenues(location, parameters, radius)

            val events = getTicketmasterEvents(location, parameters, radius)

            // Process recommendations with retrieved venues and events
            processRecommendations(
                venues = venues,  // Make sure we're passing the venues list
                events = events,
                weather = weather,
                parameters = parameters,
                location = location
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations", e)
            emptyList()
        }
    }

    private fun calculateActualDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return R * c
    }

    private fun calculateSearchRadius(accuracy: Float, maxDistance: Double): Int {
        // Ensure we have a minimum search radius regardless of accuracy
        val baseRadius = (maxDistance * 1000).coerceIn(MIN_RADIUS_METERS, MAX_RADIUS_METERS)
        return when {
            accuracy <= 50f -> baseRadius.toInt()
            accuracy <= 100f -> (baseRadius * 1.2).toInt()  // Increased multiplier
            else -> (baseRadius * 1.5).toInt()  // Increased multiplier
        }
    }

    private suspend fun getFoursquareVenues(
        location: ActivityLocation,
        parameters: MoodParameters,
        radius: Int
    ): List<FoursquareVenue> {
        return try {
            // First try with categories
            val categoriesResponse = foursquareService.searchVenues(
                apiKey = FOURSQUARE_API_KEY,  // Remove "Bearer" prefix - it's already in the key
                query = parameters.venueKeywords.take(2).joinToString(" "),
                latLong = "${location.latitude},${location.longitude}",
                radius = radius,
                categories = parameters.foursquareCategories.joinToString(",")
            )

            var venues = if (categoriesResponse.isSuccessful) {
                categoriesResponse.body()?.results ?: emptyList()
            } else {
                Log.e(TAG, "Category search failed: ${categoriesResponse.code()} - ${categoriesResponse.errorBody()?.string()}")
                emptyList()
            }

            // If no results, try without categories
            if (venues.isEmpty()) {
                val backupResponse = foursquareService.searchVenues(
                    apiKey = FOURSQUARE_API_KEY,
                    query = parameters.venueKeywords.first(),
                    latLong = "${location.latitude},${location.longitude}",
                    radius = radius,
                    categories = null
                )

                if (backupResponse.isSuccessful) {
                    venues = backupResponse.body()?.results ?: emptyList()
                } else {
                    Log.e(TAG, "Backup search failed: ${backupResponse.code()} - ${backupResponse.errorBody()?.string()}")
                }
            }

            // Try one more time with a generic query if still no results
            if (venues.isEmpty()) {
                val lastResponse = foursquareService.searchVenues(
                    apiKey = FOURSQUARE_API_KEY,
                    query = "entertainment places",
                    latLong = "${location.latitude},${location.longitude}",
                    radius = radius,
                    categories = null
                )

                if (lastResponse.isSuccessful) {
                    venues = lastResponse.body()?.results ?: emptyList()
                }
            }

            // Log final results
            venues

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Foursquare venues", e)
            emptyList()
        }
    }
    private suspend fun getTicketmasterEvents(
        location: ActivityLocation,
        parameters: MoodParameters,
        radius: Int
    ): List<Event> {
        return try {

            val response = ticketmasterService.searchEvents(
                apiKey = ApiKeys.TICKETMASTER_API_KEY,
                latLong = "${location.latitude},${location.longitude}",
                radius = "100",
                unit = "km",
                size = 50,
                sort = "date,asc",
                countryCode = "CA"
            )
            if (response.isSuccessful) {
                val events = response.body()?._embedded?.events ?: emptyList()

                events.filter { event ->
                    val venue = event._embedded?.venues?.firstOrNull()
                    venue?.location?.let { venueLoc ->
                        val distance = calculateActualDistance(
                            location.latitude, location.longitude,
                            venueLoc.latitude.toDouble(),
                            venueLoc.longitude.toDouble()
                        )
                        val withinDistance = distance <= MAX_ACTUAL_DISTANCE_KM
                        withinDistance
                    } ?: false
                }
            } else {
                Log.e(TAG, "Ticketmaster API error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Ticketmaster events", e)
            emptyList()
        }
    }

    private fun eventMatchesParameters(event: Event, parameters: MoodParameters): Boolean {
        val eventCategory = event.classifications?.firstOrNull()?.segment?.name?.lowercase() ?: ""
        val matchesCategory = parameters.eventCategories.any {
            eventCategory.contains(it.lowercase())
        }

        val eventName = event.name.lowercase()
        val matchesKeywords = parameters.eventKeywords.any { keyword ->
            eventName.contains(keyword.lowercase())
        }

        val priceInRange = when (parameters.pricePreference) {
            PricePreference.LOW -> event.priceRanges?.any { it.min <= 30.0 } ?: true
            PricePreference.MEDIUM -> event.priceRanges?.any {
                it.min > 30.0 && it.min <= 100.0
            } ?: true
            PricePreference.HIGH -> event.priceRanges?.any { it.min > 100.0 } ?: true
            PricePreference.ANY -> true
            else -> true // Fallback case
        }

        return (matchesCategory || matchesKeywords) && priceInRange
    }

    private fun processRecommendations(
        venues: List<FoursquareVenue>,
        events: List<Event>,
        weather: WeatherInfo?,
        parameters: MoodParameters,
        location: ActivityLocation
    ): List<NearbyActivity> {
        Log.d(TAG, "Starting recommendations processing with ${venues.size} venues and ${events.size} events")

        val activities = mutableListOf<NearbyActivity>()

        // Process venues
        venues.forEach { venue ->
            try {
                val activity = convertFoursquareToNearbyActivity(venue, weather)
                val venueScore = calculateVenueMoodScore(venue, parameters)

                // Lower threshold from 0.5 to 0.3 to include more venues
                if (venueScore > 0.3) {
                    activities.add(activity)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing venue ${venue.name}", e)
            }
        }

        // Process events
        events.forEach { event ->
            try {
                val activity = convertEventToNearbyActivity(event, location, weather)
                val eventScore = calculateEventMoodScore(event, parameters)

                // Lower threshold from 0.5 to 0.3 to include more events
                if (eventScore > 0.3) {
                    activities.add(activity)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing event ${event.name}", e)
            }
        }

        // Ensure we have activities before proceeding
        if (activities.isEmpty()) {
            // Take top 5 venues and top 5 events regardless of score
            val topVenues = venues.sortedByDescending { calculateVenueMoodScore(it, parameters) }.take(5)
            val topEvents = events.sortedByDescending { calculateEventMoodScore(it, parameters) }.take(5)

            topVenues.forEach { venue ->
                activities.add(convertFoursquareToNearbyActivity(venue, weather))
            }
            topEvents.forEach { event ->
                activities.add(convertEventToNearbyActivity(event, location, weather))
            }
        }

        // Sort and return final recommendations
        return activities
            .distinctBy { "${it.name}${it.type}" }
            .sortedWith(
                compareByDescending<NearbyActivity> { activity ->
                    when (activity.type) {
                        "venue" -> calculateVenueMoodScore(
                            venues.find { it.fsq_id == activity.placeId } ?: return@compareByDescending 0.0,
                            parameters
                        )
                        "event" -> calculateEventMoodScore(
                            events.find { "tm_${it.id}" == activity.id } ?: return@compareByDescending 0.0,
                            parameters
                        )
                        else -> 0.0
                    }
                }.thenBy { it.distance }
            )
            .take(10)
    }
    private fun isIndoorActivity(activity: NearbyActivity): Boolean {
        return activity.category.lowercase().let { cat ->
            cat.contains("cafe") ||
                    cat.contains("restaurant") ||
                    cat.contains("library") ||
                    cat.contains("museum") ||
                    cat.contains("mall") ||
                    cat.contains("cinema") ||
                    cat.contains("theater") ||
                    cat.contains("spa")
        }
    }

    private fun isImmediateActivity(activity: NearbyActivity): Boolean {
        // Venues are immediate, events depend on their scheduled time
        return when (activity.type) {
            "venue" -> true
            "event" -> {
                // For events, we'd check if they're happening soon
                // This is a simplified version - you'd want to check actual event times
                true
            }
            else -> false
        }
    }


    private fun getEventTimeOfDay(event: Event): String {
        // Extract hour from event start time and determine period
        return try {
            val startTime = event.dates.start.localTime
            val hour = startTime.substring(0, 2).toInt()
            when {
                hour < 12 -> "morning"
                hour < 17 -> "afternoon"
                else -> "evening"
            }
        } catch (e: Exception) {
            // Default to evening if can't determine time
            "evening"
        }
    }

    private fun getNextPeriod(currentPeriod: String): String {
        return when (currentPeriod) {
            "morning" -> "afternoon"
            "afternoon" -> "evening"
            "evening" -> "morning"
            else -> "afternoon"
        }
    }

    private fun calculateVenueMoodScore(venue: FoursquareVenue, parameters: MoodParameters): Double {
        var score = 0.0
        val category = venue.categories.firstOrNull()?.name?.lowercase() ?: return 0.0

        // Get mood from keywords
        val moodBasedScore = when {
            // Happy mood indicators
            parameters.venueKeywords.any { it in listOf("festival", "entertainment", "social", "party", "fun") } -> {
                when {
                    category.contains("entertainment") || category.contains("party") -> 0.4
                    category.contains("social") || category.contains("plaza") -> 0.3
                    category.contains("cafe") || category.contains("restaurant") -> 0.2
                    else -> 0.1
                }
            }

            // Sad mood indicators
            parameters.venueKeywords.any { it in listOf("peaceful", "quiet", "nature", "relaxing", "comfort") } -> {
                when {
                    category.contains("garden") || category.contains("park") -> 0.4
                    category.contains("wellness") || category.contains("spa") -> 0.3
                    category.contains("cafe") || category.contains("bookstore") -> 0.2
                    else -> 0.1
                }
            }

            // Excited mood indicators
            parameters.venueKeywords.any { it in listOf("adventure", "active", "sports", "energetic") } -> {
                when {
                    category.contains("sports") || category.contains("activity") -> 0.4
                    category.contains("entertainment") || category.contains("game") -> 0.3
                    category.contains("dance") || category.contains("fitness") -> 0.2
                    else -> 0.1
                }
            }

            // Relaxed mood indicators
            parameters.venueKeywords.any { it in listOf("peaceful", "calm", "serene", "cozy") } -> {
                when {
                    category.contains("spa") || category.contains("garden") -> 0.4
                    category.contains("cafe") || category.contains("tea") -> 0.3
                    category.contains("bookstore") || category.contains("library") -> 0.2
                    else -> 0.1
                }
            }

            // Bored mood indicators
            parameters.venueKeywords.any { it in listOf("interesting", "unique", "fun", "entertaining") } -> {
                when {
                    category.contains("entertainment") || category.contains("arcade") -> 0.4
                    category.contains("activity") || category.contains("game") -> 0.3
                    category.contains("shopping") || category.contains("museum") -> 0.2
                    else -> 0.1
                }
            }

            // Anxious mood indicators
            parameters.venueKeywords.any { it in listOf("quiet", "peaceful", "calming", "safe") } -> {
                when {
                    category.contains("spa") || category.contains("wellness") -> 0.4
                    category.contains("garden") || category.contains("park") -> 0.3
                    category.contains("library") || category.contains("cafe") -> 0.2
                    else -> 0.1
                }
            }

            // Focused mood indicators
            parameters.venueKeywords.any { it in listOf("quiet", "study", "work", "productive") } -> {
                when {
                    category.contains("library") || category.contains("study") -> 0.4
                    category.contains("workspace") || category.contains("coworking") -> 0.3
                    category.contains("cafe") || category.contains("coffee") -> 0.2
                    else -> 0.1
                }
            }

            else -> 0.1 // Default score
        }

        score += moodBasedScore

        // Time of day adjustment
        when (getPeriodOfDay()) {
            "morning" -> {
                if (category.contains("coffee") || category.contains("breakfast")) score += 0.2
            }
            "afternoon" -> {
                if (category.contains("park") || category.contains("shopping")) score += 0.2
            }
            "evening" -> {
                if (category.contains("restaurant") || category.contains("entertainment")) score += 0.2
            }
        }

        // Weather adjustment
        if (parameters.weatherSensitive) {
            val isIndoorVenue = isIndoorVenue(category)
            score *= if (isIndoorVenue) 1.2 else 0.8
        }

        // Distance adjustment
        if (venue.distance <= parameters.maxDistance * 500) { // If within half the max distance
            score *= 1.2
        }

        return score.coerceIn(0.0, 1.0)
    }
    private fun calculateEventMoodScore(event: Event, parameters: MoodParameters): Double {
        var score = 0.0
        val category = event.classifications?.firstOrNull()?.segment?.name?.lowercase() ?: return 0.0
        val eventName = event.name.lowercase()
        val currentMood = getMoodFromParameters(parameters)

        // Mood-specific event scoring
        when (currentMood.lowercase()) {
            "happy" -> {
                if (category.contains("music") || category.contains("comedy")) score += 0.4
                if (category.contains("family") || category.contains("festival")) score += 0.3
                if (eventName.contains("party") || eventName.contains("celebration")) score += 0.3
            }
            "relaxed" -> {
                if (category.contains("art") || category.contains("cultural")) score += 0.4
                if (category.contains("exhibition") || category.contains("museum")) score += 0.4
                if (eventName.contains("acoustic") || eventName.contains("jazz")) score += 0.3
            }
            // Add more mood cases here as needed
        }

        // Time of day adjustment
        val eventTime = getEventTimeOfDay(event)
        score *= when {
            eventTime == getPeriodOfDay() -> 1.2
            eventTime == getNextPeriod(getPeriodOfDay()) -> 1.1
            else -> 0.9
        }

        return score.coerceIn(0.0, 1.0)
    }
    private fun buildBalancedRecommendations(
        groupedActivities: Map<String, List<NearbyActivity>>,
        parameters: MoodParameters
    ): List<NearbyActivity> {
        val recommendations = mutableListOf<NearbyActivity>()

        // Add high priority activities first
        groupedActivities["high"]?.let { highPriority ->
            recommendations.addAll(
                highPriority.sortedWith(
                    compareByDescending<NearbyActivity> {
                        if (it.type == "venue") calculateVenuePriority(it, parameters)
                        else calculateEventPriority(it, parameters)
                    }
                ).take(5)
            )
        }

        // Fill remaining slots with medium priority activities
        val remainingSlots = 10 - recommendations.size
        if (remainingSlots > 0) {
            groupedActivities["medium"]?.let { mediumPriority ->
                recommendations.addAll(
                    mediumPriority.sortedWith(
                        compareByDescending<NearbyActivity> {
                            if (it.type == "venue") calculateVenuePriority(it, parameters)
                            else calculateEventPriority(it, parameters)
                        }
                    ).take(remainingSlots)
                )
            }
        }

        return recommendations.distinctBy { "${it.name}${it.type}" }
            .take(10)
    }


    private fun calculateVenuePriority(activity: NearbyActivity, parameters: MoodParameters): Double {
        var priority = 0.0

        // Distance factor (closer is better)
        priority += (parameters.maxDistance - activity.distance) / parameters.maxDistance

        // Category match
        if (isHighPriorityForMood(activity, parameters)) priority += 0.4
        else if (isMediumPriorityForMood(activity, parameters)) priority += 0.2

        // Indoor/outdoor preference
        if (parameters.preferIndoor == isIndoorActivity(activity)) priority += 0.2

        return priority
    }

    private fun calculateEventPriority(activity: NearbyActivity, parameters: MoodParameters): Double {
        var priority = 0.0

        // Time match
        if (parameters.timeOfDayPreference.contains(getPeriodOfDay())) priority += 0.3

        // Category match
        if (isHighPriorityForMood(activity, parameters)) priority += 0.4
        else if (isMediumPriorityForMood(activity, parameters)) priority += 0.2

        // Indoor/outdoor preference
        if (parameters.preferIndoor == isIndoorActivity(activity)) priority += 0.2

        return priority
    }

    private fun activityMatchesParameters(
        activity: NearbyActivity,
        parameters: MoodParameters,
        weather: WeatherInfo?
    ): Boolean {
        // Check weather sensitivity
        if (parameters.weatherSensitive && weather != null) {
            if (!parameters.preferIndoor && !weather.isGoodForActivity) {
                return false
            }
        }

        // Check distance
        if (activity.distance > parameters.maxDistance) {
            return false
        }

        // Check popularity if preferred
        if (parameters.preferPopular && (activity.rating ?: 0f) < 4.0f) {
            return false
        }

        return true
    }

    private fun convertFoursquareToNearbyActivity(
        venue: FoursquareVenue,
        weather: WeatherInfo?
    ): NearbyActivity {
        val category = venue.categories.firstOrNull()?.name ?: "Place"
        val distance = venue.distance / 1000.0 // Convert meters to kilometers

        return NearbyActivity(
            id = "fsq_${venue.fsq_id}",
            name = venue.name,
            type = "venue",
            category = category,
            distance = distance,
            address = venue.location.formatted_address,
            rating = venue.rating?.toFloat(),
            imageUrl = venue.photos?.firstOrNull()?.let {
                "${it.prefix}original${it.suffix}"
            },
            openNow = true,
            weather = weather,
            placeId = venue.fsq_id,
            externalLink = "https://foursquare.com/v/${venue.fsq_id}"
        )
    }

    private fun convertEventToNearbyActivity(
        event: Event,
        location: ActivityLocation,
        weather: WeatherInfo?
    ): NearbyActivity {
        val venue = event._embedded?.venues?.firstOrNull()
        val venueLocation = venue?.location

        val distance = if (venueLocation != null) {
            calculateDistance(
                lat1 = location.latitude,
                lon1 = location.longitude,
                lat2 = venueLocation.latitude,
                lon2 = venueLocation.longitude
            )
        } else 0.0

        return NearbyActivity(
            id = "tm_${event.id}",
            name = event.name,
            type = "event",
            category = event.classifications?.firstOrNull()?.segment?.name ?: "Event",
            distance = distance,
            address = buildEventAddress(venue),
            rating = null,
            imageUrl = event.images.maxByOrNull { it.width * it.height }?.url,
            openNow = true,
            weather = weather,
            externalLink = event.url,
            placeId = null
        )
    }

    private fun buildEventAddress(venue: Venue?): String {
        return if (venue != null) {
            listOfNotNull(
                venue.name,
                venue.address?.line1,
                venue.city?.name,
                venue.state?.name
            ).joinToString(", ")
        } else ""
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
                        isGoodForActivity = isWeatherSuitableForActivity(
                            weatherResponse,
                            weather?.description
                        )
                    )
                }
            } else {
                Log.e(TAG, "Weather API error: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather", e)
            null
        }
    }

    private fun isWeatherSuitableForActivity(
        weatherResponse: WeatherResponse,
        description: String?
    ): Boolean {
        if (description == null) return true

        // Check for bad weather conditions
        val badConditions = listOf("rain", "snow", "storm", "thunder", "heavy", "sleet", "hail")
        if (badConditions.any { description.lowercase().contains(it) }) {
            return false
        }

        // Check temperature (too cold or too hot)
        if (weatherResponse.main.temp < 5 || weatherResponse.main.temp > 35) {
            return false
        }

        // Check wind speed (too windy)
        if (weatherResponse.wind.speed > 10.0) { // 10 m/s is quite windy
            return false
        }

        // Check visibility (too foggy or poor visibility)
        if (weatherResponse.visibility < 1000) { // less than 1km visibility
            return false
        }

        return true
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return round(R * c * 100) / 100 // Round to 2 decimal places
    }

    private fun shouldIncludeActivity(
        activity: NearbyActivity,
        parameters: MoodParameters,
        weather: WeatherInfo?
    ): Boolean {
        // Distance check
        if (activity.distance > parameters.maxDistance * 1.5) {
            return false
        }

        // Weather check for outdoor venues
        if (parameters.weatherSensitive && weather != null && !parameters.preferIndoor) {
            val isOutdoorVenue = activity.category.lowercase().let { cat ->
                cat.contains("park") || cat.contains("outdoor") || cat.contains("garden")
            }

            if (isOutdoorVenue && !weather.isGoodForActivity) {
                return false
            }
        }

        // Modified popularity check - only apply to venues, not events
        if (parameters.preferPopular && activity.type == "venue") {
            val rating = activity.rating ?: 0f
            if (rating < 3.5f) {
                return false
            }
        }

        // Time of day check
        val currentPeriod = getPeriodOfDay()
        if (!parameters.timeOfDayPreference.contains(currentPeriod)) {
            return false
        }

        // Check if the activity category or name matches any of the mood keywords
        val keywordMatch = parameters.venueKeywords.any { keyword ->
            activity.category.lowercase().contains(keyword.lowercase()) ||
                    activity.name.lowercase().contains(keyword.lowercase()) ||
                    // For events, also check event categories
                    (activity.type == "event" && parameters.eventCategories.any { cat ->
                        activity.category.lowercase().contains(cat.lowercase())
                    })
        }

        if (!keywordMatch) {
            return false
        }

        return true
    }

    private fun enhanceActivityWithContextualInfo(
        activity: NearbyActivity,
        weather: WeatherInfo?,
        parameters: MoodParameters
    ): NearbyActivity {
        val calendar = Calendar.getInstance()
        val isEveningOrNight = calendar.get(Calendar.HOUR_OF_DAY) >= 18

        val contextualTips = mutableListOf<String>()

        // Weather-based tips
        weather?.let {
            when {
                it.temperature > 30 -> contextualTips.add("It's hot outside, consider bringing water")
                it.temperature < 10 -> contextualTips.add("It's cold, dress warmly")
                it.description.contains("rain", ignoreCase = true) ->
                    contextualTips.add("Rain expected, bring an umbrella")
                else -> {} // No specific weather tip needed
            }
        }

        // Time-based tips
        if (isEveningOrNight) {
            contextualTips.add("Evening activity - check closing hours")
        }

        // Distance-based tips
        if (activity.distance > 5.0) {
            contextualTips.add("Consider transportation options")
        }

        // Category-specific tips
        when {
            activity.category.contains("Park", ignoreCase = true) ->
                contextualTips.add("Great for outdoor activities")
            activity.category.contains("Restaurant", ignoreCase = true) ->
                contextualTips.add("Consider making a reservation")
            activity.category.contains("Museum", ignoreCase = true) ->
                contextualTips.add("Check for guided tours")
            activity.category.contains("Event", ignoreCase = true) ->
                contextualTips.add("Book tickets in advance")
            activity.category.contains("Coffee", ignoreCase = true) ->
                contextualTips.add("Perfect for a relaxing break")
            activity.category.contains("Library", ignoreCase = true) ->
                contextualTips.add("Quiet environment for focus")
            activity.category.contains("Fitness", ignoreCase = true) ->
                contextualTips.add("Bring workout gear")
            activity.category.contains("Shopping", ignoreCase = true) ->
                contextualTips.add("Check store hours")
            activity.category.contains("Art", ignoreCase = true) ->
                contextualTips.add("Look for guided tours or exhibitions")
            activity.category.contains("Sports", ignoreCase = true) ->
                contextualTips.add("Check event schedule")
            else -> {} // No specific category tip needed
        }

        // Add mood-specific tips
        when (parameters.preferIndoor) {
            true -> {
                if (!isIndoorVenue(activity.category)) {
                    contextualTips.add("Consider indoor alternatives if weather changes")
                }
            }
            false -> {
                if (isIndoorVenue(activity.category)) {
                    contextualTips.add("Consider outdoor options nearby")
                }
            }
        }

        return activity.copy(contextualTips = contextualTips)
    }
    private fun isIndoorVenue(category: String): Boolean {
        val indoorCategories = listOf(
            "museum", "restaurant", "library", "coffee", "shopping",
            "fitness", "theater", "cinema", "gallery"
        )
        return indoorCategories.any { category.lowercase().contains(it) }
    }

    // Update getPeriodOfDay to use when with else
    private fun getPeriodOfDay(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "morning"
            hour < 17 -> "afternoon"
            else -> "evening"
        }
    }
    data class RecommendationMetrics(
        val weatherScore: Double,
        val distanceScore: Double,
        val relevanceScore: Double,
        val popularityScore: Double
    )

    private fun calculateRecommendationScore(
        activity: NearbyActivity,
        parameters: MoodParameters,
        weather: WeatherInfo?
    ): Double {
        val baseScore = listOf(
            calculateWeatherScore(activity, weather, parameters) * 0.2,
            calculateDistanceScore(activity, parameters.maxDistance) * 0.2,
            calculateTimeScore(activity, parameters.timeOfDayPreference) * 0.15,
            calculatePopularityScore(activity, parameters.preferPopular) * 0.15,
            calculateCategoryMatchScore(activity, parameters) * 0.3
        ).sum()

        // Apply mood-specific adjustments
        val moodMultiplier = when (activity.category.lowercase()) {
            // Boost scores for particularly good matches with the mood
            in listOf("library", "study", "coworking") ->
                if (parameters.timeOfDayPreference.contains("morning")) 1.2 else 1.0
            in listOf("nightlife", "bar", "club") ->
                if (parameters.timeOfDayPreference.contains("evening")) 1.2 else 0.8
            in listOf("park", "garden") ->
                if (weather?.isGoodForActivity == true) 1.2 else 0.7
            else -> 1.0
        }

        return baseScore * moodMultiplier
    }

    private fun calculateCategoryMatchScore(
        activity: NearbyActivity,
        parameters: MoodParameters
    ): Double {
        val categoryMatch = parameters.venueKeywords.any { keyword ->
            activity.category.lowercase().contains(keyword.lowercase())
        }
        val nameMatch = parameters.venueKeywords.any { keyword ->
            activity.name.lowercase().contains(keyword.lowercase())
        }

        return when {
            categoryMatch && nameMatch -> 1.0
            categoryMatch -> 0.8
            nameMatch -> 0.6
            else -> 0.4
        }
    }

    private fun calculateTimeScore(
        activity: NearbyActivity,
        preferredTimes: List<String>
    ): Double {
        val currentPeriod = getPeriodOfDay()
        return if (preferredTimes.contains(currentPeriod)) 1.0 else 0.5
    }


    private fun calculateWeatherScore(
        activity: NearbyActivity,
        weather: WeatherInfo?,
        parameters: MoodParameters
    ): Double {
        if (!parameters.weatherSensitive || weather == null) return 1.0

        val isOutdoorVenue = listOf("park", "garden", "plaza", "outdoor")
            .any { activity.category.lowercase().contains(it) }

        return if (isOutdoorVenue) {
            if (weather.isGoodForActivity) 1.0 else 0.3
        } else {
            if (weather.isGoodForActivity) 0.8 else 1.0
        }
    }

    private fun calculateDistanceScore(activity: NearbyActivity, maxDistance: Double): Double {
        return 1.0 - (activity.distance / maxDistance).coerceIn(0.0, 1.0)
    }

    private fun calculateRelevanceScore(
        activity: NearbyActivity,
        parameters: MoodParameters
    ): Double {
        val keywordMatch = parameters.venueKeywords.count { keyword ->
            activity.name.lowercase().contains(keyword) ||
                    activity.category.lowercase().contains(keyword)
        }
        return (keywordMatch.toDouble() / parameters.venueKeywords.size).coerceIn(0.0, 1.0)
    }

    private fun calculatePopularityScore(
        activity: NearbyActivity,
        preferPopular: Boolean
    ): Double {
        if (!preferPopular) return 1.0
        return (activity.rating ?: 0f) / 5.0
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