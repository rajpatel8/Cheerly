package com.rajkumar.cheerly

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class YouTubeRepository {
    private val API_KEY = "AIzaSyARuNG3izYaKb5ao8nErJd9tKqupYrF3-A" // Replace with your API key
    private val BASE_URL = "https://www.googleapis.com/"

    private val youtubeService: YouTubeService

    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
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
                val queryParams = when (mood.lowercase()) {
                    "happy" -> listOf(
                        "upbeat positive songs ${getRandomGenre()}",
                        "happy ${getRandomGenre()} playlist",
                        "feel good ${getRandomLanguage()} songs",
                        "motivational ${getRandomGenre()} mix",
                        "cheerful ${getRandomDecade()} hits"
                    )
                    "sad" -> listOf(
                        "calming ${getRandomGenre()} songs",
                        "peaceful ${getRandomLanguage()} music",
                        "soothing ${getRandomDecade()} playlist",
                        "relaxing ${getRandomGenre()} mix",
                        "gentle acoustic songs"
                    )
                    "excited" -> listOf(
                        "energetic ${getRandomGenre()} mix",
                        "pump up ${getRandomLanguage()} songs",
                        "workout ${getRandomGenre()} playlist",
                        "party ${getRandomDecade()} hits",
                        "high energy ${getRandomGenre()} music"
                    )
                    "relaxed" -> listOf(
                        "chill ${getRandomGenre()} vibes",
                        "ambient ${getRandomLanguage()} songs",
                        "meditation ${getRandomGenre()} music",
                        "lofi ${getRandomDecade()} mix",
                        "peaceful instrumental playlist"
                    )
                    else -> listOf("mood music playlist")
                }

                val allVideos = mutableListOf<Video>()

                // Randomly select and shuffle queries
                val shuffledQueries = queryParams.shuffled()

                for (query in shuffledQueries) {
                    val response = youtubeService.searchVideos(
                        part = "snippet",
                        maxResults = 5,
                        query = "$query official",
                        type = "video",
                        videoCategoryId = "10", // Music category
                        videoDuration = "medium",
                        order = getRandomOrder(), // Random sort order
                        apiKey = API_KEY
                    )

                    if (response.isSuccessful) {
                        val videos = response.body()?.items?.map { youtubeVideo ->
                            Video(
                                id = youtubeVideo.id.videoId,
                                title = youtubeVideo.snippet.title,
                                channelName = youtubeVideo.snippet.channelTitle,
                                thumbnailUrl = youtubeVideo.snippet.thumbnails.medium.url,
                                videoUrl = "https://www.youtube.com/watch?v=${youtubeVideo.id.videoId}"
                            )
                        } ?: emptyList()

                        // Filter videos
                        val filteredVideos = videos.filter { video ->
                            !video.title.contains(Regex("(tutorial|lesson|how to|review|unboxing)", RegexOption.IGNORE_CASE)) &&
                                    !video.title.contains(Regex("(\\!{2,}|\\?{2,}|BEST EVER|100%|\\[.*\\])", RegexOption.IGNORE_CASE))
                        }

                        allVideos.addAll(filteredVideos)
                    }

                    if (allVideos.size >= 10) break
                }

                // Return random selection of videos
                allVideos.distinctBy { it.id }
                    .shuffled()
                    .take(3)
                    .also { videos ->
                        Log.d("YouTubeRepository", "Returning ${videos.size} videos")
                    }

            } catch (e: Exception) {
                Log.e("YouTubeRepository", "Error fetching videos", e)
                emptyList()
            }
        }
    }

    private fun getRandomGenre(): String {
        return listOf(
            "pop", "rock", "indie", "electronic", "hip hop",
            "jazz", "classical", "acoustic", "folk", "r&b"
        ).random()
    }

    private fun getRandomLanguage(): String {
        return listOf(
            "English", "Hindi", "Spanish",
            "Japanese", "French", "International"
        ).random()
    }

    private fun getRandomDecade(): String {
        return listOf(
            "2020s", "2010s", "2000s", "90s", "80s"
        ).random()
    }

    private fun getRandomOrder(): String {
        return listOf(
            "relevance",
            "viewCount",
            "rating",
            "date"
        ).random()
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