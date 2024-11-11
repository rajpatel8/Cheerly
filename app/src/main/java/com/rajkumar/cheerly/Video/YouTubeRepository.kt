package com.rajkumar.cheerly.Video

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class YouTubeRepository {
    private val API_KEY = "952927675552-f5sa80ckj41lemttj3cr59kfpmu0rupg.apps.googleusercontent.com" // Replace with your API key
    private val BASE_URL = "https://www.googleapis.com/"

    private val youtubeService: YouTubeService

    init {
        val okHttpClient = OkHttpClient.Builder()

            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        youtubeService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeService::class.java)
    }

    suspend fun getVideoRecommendations(mood: String): List<Video> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("YouTubeRepository", "Getting videos for mood: $mood")

                // Dynamic query parameters based on mood
                val query = when (mood.lowercase()) {
                    "happy" -> "happy upbeat music videos"
                    "sad" -> "calming music videos"
                    "excited" -> "energetic pump up music videos"
                    "relaxed" -> "chill lofi music videos"
                    else -> "music videos"
                }

                val response = youtubeService.searchVideos(
                    part = "snippet",
                    maxResults = 3,
                    query = query,
                    type = "video",
                    videoCategoryId = "10", // Music category
                    apiKey = API_KEY
                )

                if (response.isSuccessful) {
                    response.body()?.items?.map { youtubeVideo ->
                        val videoId = youtubeVideo.id.videoId
                        Video(
                            id = videoId,
                            title = youtubeVideo.snippet.title,
                            channelName = youtubeVideo.snippet.channelTitle,
                            thumbnailUrl = youtubeVideo.snippet.thumbnails.high.url,
                            videoUrl = "https://www.youtube.com/watch?v=$videoId"
                        )
                    } ?: emptyList()
                } else {
                    Log.e("YouTubeRepository", "API call failed: ${response.code()}")
                    emptyList()
                }

            } catch (e: Exception) {
                Log.e("YouTubeRepository", "Error fetching videos", e)
                emptyList()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: YouTubeRepository? = null

        fun getInstance(): YouTubeRepository {
            return instance ?: synchronized(this) {
                instance ?: YouTubeRepository().also { instance = it }
            }
        }
    }
}