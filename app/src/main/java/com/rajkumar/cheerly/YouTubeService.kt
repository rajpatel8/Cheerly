package com.rajkumar.cheerly

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeService {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 3,
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String? = null,
        @Query("videoDuration") videoDuration: String? = null,
        @Query("order") order: String? = null,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>
}

data class YouTubeSearchResponse(
    val items: List<YouTubeVideo> = emptyList()
)

data class YouTubeVideo(
    val id: VideoId,
    val snippet: VideoSnippet
)

data class VideoId(
    val videoId: String
)

data class VideoSnippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val medium: ThumbnailInfo
)

data class ThumbnailInfo(
    val url: String,
    val width: Int,
    val height: Int
)