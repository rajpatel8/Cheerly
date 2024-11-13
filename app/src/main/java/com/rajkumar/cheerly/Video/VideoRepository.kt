package com.rajkumar.cheerly.Video

import android.util.Log
import com.rajkumar.cheerly.MoodRecommendationActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository private constructor() {
    private val videoService = VideoService.getInstance()
    private val API_KEY = "AIzaSyAmAk1_sVwApr4rmxvNJ-XRLlGaQFGOA78" // Replace with your YouTube API key

    private val moodQueries = mapOf(
        "happy" to listOf(
            "uplifting happy music videos",
            "positive feel good content",
            "cheerful entertainment videos",
            "happy mood playlist"
        ),
        "sad" to listOf(
            "calming relaxing music",
            "peaceful meditation videos",
            "soothing ambient music",
            "gentle acoustic songs"
        ),
        "excited" to listOf(
            "high energy music videos",
            "epic moments compilation",
            "exciting adventure videos",
            "party music playlist"
        ),
        "relaxed" to listOf(
            "chill lofi music",
            "relaxing nature sounds",
            "peaceful piano music",
            "ambient relaxation videos"
        ),
        "bored" to listOf(
            "interesting facts videos",
            "amazing discoveries compilation",
            "mind blowing content",
            "entertaining highlights"
        ),
        "anxious" to listOf(
            "calming meditation music",
            "anxiety relief sounds",
            "peaceful nature scenes",
            "relaxing sleep music"
        ),
        "focused" to listOf(
            "study music concentration",
            "productivity music mix",
            "focus beats playlist",
            "concentration music"
        )
    )

    suspend fun getVideoRecommendations(mood: String): List<Video> = withContext(Dispatchers.IO) {
        try {
            val queries = moodQueries[mood.lowercase()] ?: moodQueries["happy"]!!
            val allVideos = mutableListOf<Video>()

            // Get videos for each query
            queries.forEach { query ->
                try {
                    val response = videoService.searchVideos(
                        query = query,
                        maxResults = 5,
                        apiKey = API_KEY
                    )

                    if (response.isSuccessful) {
                        response.body()?.items?.forEach { video ->
                            allVideos.add(
                                Video(
                                    id = video.id.videoId,
                                    title = video.snippet.title,
                                    channelName = video.snippet.channelTitle,
                                    thumbnailUrl = video.snippet.thumbnails.high.url,
                                    videoUrl = "https://www.youtube.com/watch?v=${video.id.videoId}"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching videos for query: $query", e)
                }
            }

            // Filter duplicates and select top results
            allVideos.distinctBy { it.id }
                .distinctBy { it.channelName }
                .take(5)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting video recommendations", e)
            emptyList()
        }
    }

    companion object {
        private const val TAG = "VideoRepository"

        @Volatile
        private var instance: VideoRepository? = null

        fun getInstance(moodRecommendationActivity: MoodRecommendationActivity): VideoRepository {
            return instance ?: synchronized(this) {
                instance ?: VideoRepository().also { instance = it }
            }
        }
    }
}