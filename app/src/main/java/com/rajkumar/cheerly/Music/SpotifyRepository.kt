package com.rajkumar.cheerly.Music

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import net.openid.appauth.AuthState

class SpotifyRepository(private val context: Context) {
    private val BASE_API_URL = "https://api.spotify.com/v1/"
    private val PREFS_NAME = "SpotifyAuthPrefs"
    private val KEY_AUTH_STATE = "auth_state"

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
                null
            }
        } else null
    }

    suspend fun getUserTopTracks(): List<Track> = withContext(Dispatchers.IO) {
        try {
            val token = getStoredAuthToken() ?: return@withContext emptyList()

            val response = apiService.getTopTracks(
                auth = "Bearer $token",
                limit = 5,
                timeRange = "medium_term"
            )

            if (response.isSuccessful) {
                response.body()?.items ?: emptyList()
            } else {
                throw Exception("Failed to get top tracks: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getUserRecentTracks(): List<Track> = withContext(Dispatchers.IO) {
        try {
            val token = getStoredAuthToken() ?: return@withContext emptyList()

            val response = apiService.getRecentlyPlayed(
                auth = "Bearer $token",
                limit = 10
            )

            if (response.isSuccessful) {
                response.body()?.items?.map { it.track } ?: emptyList()
            } else {
                throw Exception("Failed to get recent tracks: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRecommendations(mood: String): List<Track> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getStoredAuthToken() ?: throw Exception("No auth token available")

                // Get user's personalization data
                val topTracks = getUserTopTracks()
                val recentTracks = getUserRecentTracks()

                // Get seed tracks from user's history
                val seedTracks = (topTracks + recentTracks)
                    .distinctBy { it.id }
                    .take(2)
                    .map { it.id }
                    .joinToString(",")

                // Define mood parameters
                val (valence, energy, genres) = when (mood.lowercase()) {
                    "happy" -> Triple(0.8f, 0.7f, "pop,happy,dance")
                    "sad" -> Triple(0.2f, 0.3f, "acoustic,sad,indie")
                    "excited" -> Triple(0.6f, 0.9f, "dance,edm,party")
                    "relaxed" -> Triple(0.5f, 0.2f, "ambient,chill,study")
                    else -> Triple(0.5f, 0.5f, "pop")
                }

                // Get personalized recommendations
                val response = apiService.getRecommendations(
                    auth = "Bearer $token",
                    seedTracks = if (seedTracks.isNotEmpty()) seedTracks else null,
                    seedGenres = genres,
                    targetValence = valence,
                    targetEnergy = energy,
                    limit = 3,
                    market = "US"
                )

                if (response.isSuccessful) {
                    response.body()?.tracks ?: emptyList()
                } else {
                    throw Exception("Failed to get recommendations: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
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

// Updated service interface

// Add new response data classes

