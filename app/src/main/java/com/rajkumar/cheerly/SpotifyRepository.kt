package com.rajkumar.cheerly

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SpotifyRepository {
    private val CLIENT_ID = "e4350e0b977f4e589509378a28cebffa"  // Replace with your client ID
    private val CLIENT_SECRET = "0f0f9f8717af45319dca07497937ef54"  // Replace with your client secret
    private val BASE_AUTH_URL = "https://accounts.spotify.com/"
    private val BASE_API_URL = "https://api.spotify.com/v1/"

    private val authService: SpotifyAuthService
    private val apiService: SpotifyApiService
    private var accessToken: String? = null

    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        authService = Retrofit.Builder()
            .baseUrl(BASE_AUTH_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyAuthService::class.java)

        apiService = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApiService::class.java)
    }

    private suspend fun getAccessToken(): String {
        return accessToken ?: withContext(Dispatchers.IO) {
            val credentials = Credentials.basic(CLIENT_ID, CLIENT_SECRET)
            val response = authService.getAccessToken(credentials)

            if (response.isSuccessful) {
                response.body()?.access_token?.also {
                    accessToken = it
                } ?: throw Exception("Token response was null")
            } else {
                throw Exception("Failed to get access token: ${response.code()}")
            }
        }
    }

    suspend fun getRecommendations(mood: String): List<Track> {
        return withContext(Dispatchers.IO) {
            try {
                // Get access token
                val token = getAccessToken()

                // Define mood parameters
                val (valence, energy, genres) = when (mood.lowercase()) {
                    "happy" -> Triple(0.8f, 0.7f, "pop,happy")
                    "sad" -> Triple(0.2f, 0.3f, "acoustic,sad")
                    "excited" -> Triple(0.6f, 0.9f, "dance,party")
                    "relaxed" -> Triple(0.5f, 0.2f, "ambient,chill")
                    else -> Triple(0.5f, 0.5f, "pop")
                }

                // Make API call
                val response = apiService.getRecommendations(
                    auth = "Bearer $token",
                    seedGenres = genres,
                    targetValence = valence,
                    targetEnergy = energy
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

        fun getInstance(): SpotifyRepository {
            return instance ?: synchronized(this) {
                instance ?: SpotifyRepository().also { instance = it }
            }
        }
    }
}