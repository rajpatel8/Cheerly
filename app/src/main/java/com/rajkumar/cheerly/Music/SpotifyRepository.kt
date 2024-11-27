package com.rajkumar.cheerly.Music

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import net.openid.appauth.AuthState
import android.util.Log

class SpotifyRepository(private val context: Context) {
    private val BASE_API_URL = "https://api.spotify.com/v1/"
    private val PREFS_NAME = "SpotifyAuthPrefs"
    private val KEY_AUTH_STATE = "auth_state"
    private val TAG = "SpotifyRepository"

    // Define mood-based audio features
    data class MoodParameters(
        val valence: Float,      // Musical positiveness
        val energy: Float,       // Intensity and activity
        val danceability: Float, // How suitable for dancing
        val tempo: Float,        // Speed of the track
        val acousticness: Float, // Amount of acoustic sound
        val instrumentalness: Float, // Amount of instrumental content
        val genres: List<String>,
        val minPopularity: Int,
        val maxDurationMs: Int
    )

    private val moodParameters = mapOf(
        "happy" to MoodParameters(
            valence = 0.8f,
            energy = 0.7f,
            danceability = 0.7f,
            tempo = 120f,
            acousticness = 0.3f,
            instrumentalness = 0.2f,
            genres = listOf("pop", "happy", "dance", "electronic"),
            minPopularity = 60,
            maxDurationMs = 300000 // 5 minutes
        ),
        "sad" to MoodParameters(
            valence = 0.2f,
            energy = 0.3f,
            danceability = 0.4f,
            tempo = 90f,
            acousticness = 0.7f,
            instrumentalness = 0.4f,
            genres = listOf("acoustic", "sad", "indie", "piano"),
            minPopularity = 40,
            maxDurationMs = 360000 // 6 minutes
        ),
        "excited" to MoodParameters(
            valence = 0.8f,
            energy = 0.9f,
            danceability = 0.8f,
            tempo = 130f,
            acousticness = 0.2f,
            instrumentalness = 0.3f,
            genres = listOf("dance", "edm", "party", "electronic"),
            minPopularity = 70,
            maxDurationMs = 240000 // 4 minutes
        ),
        "relaxed" to MoodParameters(
            valence = 0.5f,
            energy = 0.2f,
            danceability = 0.3f,
            tempo = 85f,
            acousticness = 0.8f,
            instrumentalness = 0.6f,
            genres = listOf("ambient", "chill", "study", "classical"),
            minPopularity = 30,
            maxDurationMs = 420000 // 7 minutes
        ),
        "focused" to MoodParameters(
            valence = 0.5f,
            energy = 0.4f,
            danceability = 0.3f,
            tempo = 100f,
            acousticness = 0.5f,
            instrumentalness = 0.8f,
            genres = listOf("focus", "study", "classical", "ambient"),
            minPopularity = 40,
            maxDurationMs = 360000 // 6 minutes
        ),
        "anxious" to MoodParameters(
            valence = 0.4f,
            energy = 0.3f,
            danceability = 0.3f,
            tempo = 80f,
            acousticness = 0.7f,
            instrumentalness = 0.5f,
            genres = listOf("ambient", "meditation", "piano", "classical"),
            minPopularity = 30,
            maxDurationMs = 420000 // 7 minutes
        ),
        "bored" to MoodParameters(
            valence = 0.7f,
            energy = 0.6f,
            danceability = 0.6f,
            tempo = 115f,
            acousticness = 0.4f,
            instrumentalness = 0.3f,
            genres = listOf("pop", "rock", "indie", "alternative"),
            minPopularity = 50,
            maxDurationMs = 300000 // 5 minutes
        )
    )

    private val apiService: SpotifyApiService

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApiService::class.java)
    }

    private fun getStoredAuthToken(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val authStateJson = prefs.getString(KEY_AUTH_STATE, null)
        return if (authStateJson != null) {
            try {
                val authState = AuthState.jsonDeserialize(authStateJson)
                authState.accessToken
            } catch (e: Exception) {
                Log.e(TAG, "Error getting stored auth token", e)
                null
            }
        } else null
    }

    private fun getUserMusicPreferences(): Set<String> {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getStringSet("selectedMusicOptions", emptySet()) ?: emptySet()
    }

    suspend fun getRecommendations(mood: String): List<Track> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getStoredAuthToken() ?: throw Exception("No auth token available")
                Log.d(TAG, "Getting recommendations for mood: $mood")

                // Get user's music preferences from AppPrefs
                val userGenres = getUserMusicPreferences()
                Log.d(TAG, "User preferred genres: $userGenres")

                // Get user's listening history
                val userProfile = getUserProfile(mood, token)
                if (userProfile.isEmpty()) {
                    Log.w(TAG, "No user profile available, using default recommendations")
                }

                // Get mood parameters
                val params = moodParameters[mood.lowercase()] ?: moodParameters["happy"]!!
                Log.d(TAG, "Using mood parameters: $params")

                val recommendations = mutableListOf<Track>()

                // Get recommendations for each user-preferred genre
                for (genre in userGenres) {
                    val genreParams = params.copy(
                        genres = listOf(genre.lowercase()) + params.genres
                    )

                    val genreRecommendations = getRecommendationsWithSeeds(
                        token,
                        genreParams,
                        seedTracks = null,
                        seedArtists = null,
                        seedGenres = genre.lowercase()
                    )

                    // Take 3 recommendations for each genre
                    recommendations.addAll(
                        genreRecommendations
                            .distinctBy { it.id }
                            .take(3)
                    )
                }

                // If we don't have enough recommendations, add mood-based recommendations
                if (recommendations.size < userGenres.size * 3) {
                    val moodRecommendations = getRecommendationsWithSeeds(
                        token,
                        params,
                        seedTracks = userProfile.take(2).map { it.id }.joinToString(","),
                        seedArtists = null,
                        seedGenres = params.genres.take(3).joinToString(",")
                    )
                    recommendations.addAll(moodRecommendations)
                }

                // Filter and sort recommendations
                recommendations
                    .distinctBy { it.id }
                    .groupBy { track ->
                        // Find matching genre for the track
                        userGenres.find { genre ->
                            track.album.name.contains(genre, ignoreCase = true) ||
                                    track.name.contains(genre, ignoreCase = true)
                        } ?: "other"
                    }
                    .flatMap { (genre, tracks) ->
                        // Take top 3 tracks for each genre
                        tracks.sortedWith(
                            compareByDescending<Track> { it.popularity }
                                .thenBy { it.name }
                        ).take(3)
                    }
                    .also {
                        Log.d(TAG, "Returning ${it.size} recommendations")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error getting recommendations", e)
                emptyList()
            }
        }
    }

    private suspend fun getUserProfile(mood: String, token: String): List<Track> {
        val profile = mutableListOf<Track>()

        try {
            // Get user's top tracks
            val topTracks = getUserTopTracks(token)
            profile.addAll(topTracks)

            // Get recently played tracks
            val recentTracks = getUserRecentTracks(token)
            profile.addAll(recentTracks)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
        }

        return profile.distinctBy { it.id }
    }

    private suspend fun getRecommendationsWithSeeds(
        token: String,
        params: MoodParameters,
        seedTracks: String?,
        seedArtists: String?,
        seedGenres: String?
    ): List<Track> {
        try {
            val response = apiService.getRecommendations(
                auth = "Bearer $token",
                seedTracks = seedTracks,
                seedArtists = seedArtists,
                seedGenres = seedGenres,
                targetValence = params.valence,
                targetEnergy = params.energy,
                targetDanceability = params.danceability,
                targetTempo = params.tempo,
                targetAcousticness = params.acousticness,
                targetInstrumentalness = params.instrumentalness,
                minPopularity = params.minPopularity,
                maxDurationMs = params.maxDurationMs,
                limit = 10,
                market = "IN"
            )

            return if (response.isSuccessful) {
                response.body()?.tracks ?: emptyList()
            } else {
                Log.e(TAG, "Failed to get recommendations: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations with seeds", e)
            return emptyList()
        }
    }

    private suspend fun getUserTopTracks(token: String): List<Track> {
        val response = apiService.getTopTracks(
            auth = "Bearer $token",
            limit = 10,
            timeRange = "long_term"
        )
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else {
            emptyList()
        }
    }

    private suspend fun getUserRecentTracks(token: String): List<Track> {
        val response = apiService.getRecentlyPlayed(
            auth = "Bearer $token",
            limit = 10
        )
        return if (response.isSuccessful) {
            response.body()?.items?.map { it.track } ?: emptyList()
        } else {
            emptyList()
        }
    }

    companion object {
        @Volatile
        private var instance: SpotifyRepository? = null

        fun getInstance(context: Context): SpotifyRepository {
            return instance ?: synchronized(this) {
                instance ?: SpotifyRepository(context).also { instance = it }
            }
        }
    }
}