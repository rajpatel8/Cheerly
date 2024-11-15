package com.rajkumar.cheerly.Activity

import android.util.Log
import com.rajkumar.cheerly.Activity.Models.*
import com.rajkumar.cheerly.config.ApiKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.awaitAll
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
        val pricePreference: PricePreference
    )

    private val moodParameters = mapOf(
        "happy" to MoodParameters(
            foursquareCategories = listOf(
                13003, // Park
                10000, // Arts & Entertainment
                13065, // Plaza
                13338, // Nightlife
                13035  // Coffee Shop
            ),
            venueKeywords = listOf("fun", "entertainment", "social", "lively"),
            eventCategories = listOf("music", "comedy", "family", "attractions"),
            eventKeywords = listOf("festival", "carnival", "concert", "party"),
            maxDistance = 30.0,
            preferIndoor = false,
            preferPopular = true,
            weatherSensitive = true,
            timeOfDayPreference = listOf("afternoon", "evening"),
            pricePreference = PricePreference.ANY
        ),
        "sad" to MoodParameters(
            foursquareCategories = listOf(
                13003, // Park
                13091, // Garden
                12072, // Wellness
                12089, // Spa
                13035  // Coffee Shop
            ),
            venueKeywords = listOf("peaceful", "quiet", "nature", "relaxing"),
            eventCategories = listOf("art", "wellness", "museum"),
            eventKeywords = listOf("meditation", "exhibition", "workshop"),
            maxDistance = 15.0,
            preferIndoor = true,
            preferPopular = false,
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.MEDIUM
        ),
        "excited" to MoodParameters(
            foursquareCategories = listOf(
                13338, // Nightlife
                10000, // Arts & Entertainment
                18000, // Sports
                13384  // Fitness
            ),
            venueKeywords = listOf("adventure", "active", "sports", "thrilling"),
            eventCategories = listOf("sports", "music", "dance"),
            eventKeywords = listOf("competition", "game", "performance"),
            maxDistance = 40.0,
            preferIndoor = false,
            preferPopular = true,
            weatherSensitive = true,
            timeOfDayPreference = listOf("afternoon", "evening"),
            pricePreference = PricePreference.ANY
        ),
        "relaxed" to MoodParameters(
            foursquareCategories = listOf(
                13003, // Park
                13091, // Garden
                13035, // Coffee Shop
                12072  // Wellness
            ),
            venueKeywords = listOf("peaceful", "calm", "serene", "cozy"),
            eventCategories = listOf("wellness", "art", "cultural"),
            eventKeywords = listOf("meditation", "gallery", "garden"),
            maxDistance = 20.0,
            preferIndoor = false,
            preferPopular = false,
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.MEDIUM
        ),
        "focused" to MoodParameters(
            foursquareCategories = listOf(
                13035, // Coffee Shop
                12051, // Library
                13145, // Coworking Space
                13003  // Park
            ),
            venueKeywords = listOf("quiet", "study", "work", "productive"),
            eventCategories = listOf("learning", "workshop", "seminar"),
            eventKeywords = listOf("workshop", "study", "learning"),
            maxDistance = 15.0,
            preferIndoor = true,
            preferPopular = false,
            weatherSensitive = false,
            timeOfDayPreference = listOf("morning", "afternoon"),
            pricePreference = PricePreference.LOW
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

    private class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()

            Log.d("FoursquareAPI", """
                Sending request:
                URL: ${request.url}
                Headers: ${request.headers}
                Method: ${request.method}
            """.trimIndent())

            val response = chain.proceed(request)

            Log.d("FoursquareAPI", """
                Received response:
                Code: ${response.code}
                Message: ${response.message}
            """.trimIndent())

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

            // Fetch venues and events in parallel with location filtering
            val (venues, events) = withContext(Dispatchers.IO) {
                val venuesDeferred = async {
                    getFoursquareVenues(location, parameters, radius)
                }
                val eventsDeferred = async {
                    getTicketmasterEvents(location, parameters, radius)
                }
                Pair(venuesDeferred.await(), eventsDeferred.await())
            }

            // Filter out results that are too far
            val filteredVenues = venues.filter { venue ->
                val distance = calculateActualDistance(
                    location.latitude, location.longitude,
                    venue.location.lat, venue.location.lng
                )
                distance <= MAX_ACTUAL_DISTANCE_KM
            }

            val filteredEvents = events.filter { event ->
                val venue = event._embedded?.venues?.firstOrNull()
                if (venue?.location != null) {
                    val distance = calculateActualDistance(
                        location.latitude, location.longitude,
                        venue.location.latitude.toDouble(),
                        venue.location.longitude.toDouble()
                    )
                    distance <= MAX_ACTUAL_DISTANCE_KM
                } else {
                    false
                }
            }

            // Process recommendations with filtered results
            processRecommendations(
                venues = filteredVenues,
                events = filteredEvents,
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
            Log.d(TAG, """
            Searching for venues:
            Location: ${location.latitude}, ${location.longitude}
            Radius: $radius meters
            Categories: ${parameters.foursquareCategories.joinToString(",")}
        """.trimIndent())

            val categories = parameters.foursquareCategories.joinToString(",")
            val query = parameters.venueKeywords.shuffled().take(2).joinToString(" ")
            val latLong = "${location.latitude},${location.longitude}"

            val response = foursquareService.searchVenues(
                apiKey = FOURSQUARE_API_KEY,
                query = query,
                latLong = latLong,
                radius = radius,
                categories = categories,
                sort = FoursquareService.DEFAULT_SORT,
                limit = 50,
                fields = FoursquareService.DEFAULT_FIELDS
            )

            if (response.isSuccessful) {
                val venues = response.body()?.results ?: emptyList()
                Log.d(TAG, "Found ${venues.size} venues before filtering")

                venues.filter { venue ->
                    val distance = calculateActualDistance(
                        location.latitude, location.longitude,
                        venue.location.lat, venue.location.lng
                    )
                    val withinDistance = distance <= MAX_ACTUAL_DISTANCE_KM
                    Log.d(TAG, """
                    Venue: ${venue.name}
                    Distance: ${distance}km
                    Within range: $withinDistance
                    Location: ${venue.location.lat}, ${venue.location.lng}
                """.trimIndent())
                    withinDistance
                }.also {
                    Log.d(TAG, "Returning ${it.size} venues after filtering")
                }
            } else {
                Log.e(TAG, "Foursquare API error: ${response.code()} - ${response.errorBody()?.string()}")
                emptyList()
            }
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
            Log.d(TAG, """
            Searching for events:
            Location: ${location.latitude}, ${location.longitude}
            Radius: 100km
            Country: CA
        """.trimIndent())

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
                Log.d(TAG, "Found ${events.size} events before filtering")

                events.filter { event ->
                    val venue = event._embedded?.venues?.firstOrNull()
                    venue?.location?.let { venueLoc ->
                        val distance = calculateActualDistance(
                            location.latitude, location.longitude,
                            venueLoc.latitude.toDouble(),
                            venueLoc.longitude.toDouble()
                        )
                        val withinDistance = distance <= MAX_ACTUAL_DISTANCE_KM
                        if (!withinDistance) {
                            Log.d(TAG, "Filtered out event ${event.name} at distance ${distance}km")
                        }
                        withinDistance
                    } ?: false
                }.also {
                    Log.d(TAG, "Returning ${it.size} events after filtering")
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
        val activities = mutableListOf<NearbyActivity>()

        Log.d(TAG, "Processing ${venues.size} venues and ${events.size} events")

        // Process venues with less strict filtering
        venues.forEach { venue ->
            val basicActivity = convertFoursquareToNearbyActivity(venue, weather)
            // Removed activityMatchesParameters check to be more lenient
            val enhancedActivity = enhanceActivityWithContextualInfo(basicActivity, weather, parameters)
            activities.add(enhancedActivity)
        }

        // Process events with less strict filtering
        events.forEach { event ->
            val basicActivity = convertEventToNearbyActivity(event, location, weather)
            // Removed activityMatchesParameters check to be more lenient
            val enhancedActivity = enhanceActivityWithContextualInfo(basicActivity, weather, parameters)
            activities.add(enhancedActivity)
        }

        // Sort and return all recommendations, taking more results
        return activities
            .distinctBy { it.id }
            .sortedWith(
                compareBy<NearbyActivity> { -calculateRecommendationScore(it, parameters, weather) }
                    .thenBy { it.distance }
            )
            .take(10)  // Increased from 5 to 10
            .also {
                Log.d(TAG, "Returning ${it.size} final recommendations")
            }
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
        return NearbyActivity(
            id = "fsq_${venue.fsq_id}",
            name = venue.name,
            type = "venue",
            category = venue.categories.firstOrNull()?.name ?: "Place",
            distance = venue.distance / 1000.0, // Convert meters to kilometers
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
        val currentPeriod = getPeriodOfDay()

        // Time of day preference
        if (!parameters.timeOfDayPreference.contains(currentPeriod)) {
            return false
        }

        // Weather conditions for outdoor activities
        if (parameters.weatherSensitive && !parameters.preferIndoor) {
            if (weather?.isGoodForActivity == false) {
                return false
            }
        }

        // Indoor/outdoor preference based on current weather
        if (weather != null) {
            val isOutdoorVenue = listOf("park", "garden", "plaza", "outdoor")
                .any { activity.category.lowercase().contains(it) }

            if (isOutdoorVenue && !weather.isGoodForActivity && parameters.weatherSensitive) {
                return false
            }
        }

        // Popular venues preference
        if (parameters.preferPopular) {
            val hasGoodRating = activity.rating?.let { it >= 4.0f } ?: false
            if (!hasGoodRating) {
                return false
            }
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
        val metrics = RecommendationMetrics(
            weatherScore = calculateWeatherScore(activity, weather, parameters),
            distanceScore = calculateDistanceScore(activity, parameters.maxDistance),
            relevanceScore = calculateRelevanceScore(activity, parameters),
            popularityScore = calculatePopularityScore(activity, parameters.preferPopular)
        )

        return (metrics.weatherScore * 0.3 +
                metrics.distanceScore * 0.25 +
                metrics.relevanceScore * 0.25 +
                metrics.popularityScore * 0.2)
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