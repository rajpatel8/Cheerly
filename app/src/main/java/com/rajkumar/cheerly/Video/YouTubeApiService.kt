package com.rajkumar.cheerly.Video

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class YouTubeApiService(private val accessToken: String) {
    private val youtube: YouTube by lazy {
        YouTube.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        ) { request ->
            request.headers["Authorization"] = "Bearer $accessToken"
        }.setApplicationName("Cheerly").build()
    }

    suspend fun getSubscribedChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val request = youtube.subscriptions()
                .list(listOf("snippet"))
                .setMine(true)
                .setMaxResults(50L)

            val response = request.execute()
            response.items?.map { subscription ->
                Channel(
                    id = subscription.snippet.resourceId.channelId,
                    title = subscription.snippet.title,
                    description = subscription.snippet.description,
                    thumbnailUrl = subscription.snippet.thumbnails.default.url
                )
            } ?: emptyList()
        } catch (e: GoogleJsonResponseException) {
            throw YouTubeApiException("Failed to get subscribed channels", e)
        }
    }

    suspend fun getLikedVideos(): List<Video> = withContext(Dispatchers.IO) {
        try {
            val request = youtube.videos()
                .list(listOf("snippet", "contentDetails", "statistics"))
                .setMyRating("like")
                .setMaxResults(50L)

            val response = request.execute()
            response.items?.map { video ->
                val videoId = video.id
                Video(
                    id = videoId,
                    title = video.snippet.title,
                    channelName = video.snippet.channelTitle,
                    thumbnailUrl = video.snippet.thumbnails.default.url,
                    videoUrl = "https://www.youtube.com/watch?v=$videoId"
                )
            } ?: emptyList()
        } catch (e: GoogleJsonResponseException) {
            throw YouTubeApiException("Failed to get liked videos", e)
        }
    }
}

data class Channel(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String
)

class YouTubeApiException(message: String, cause: Throwable? = null) : Exception(message, cause)