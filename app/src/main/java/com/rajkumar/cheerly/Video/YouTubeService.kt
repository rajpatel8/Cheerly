package com.rajkumar.cheerly.Video

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface YouTubeApi {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int,
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("key") apiKey: String,
        @Query("videoDuration") videoDuration: String? = null,
        @Query("relevanceLanguage") relevanceLanguage: String = "en",
        @Query("safeSearch") safeSearch: String = "moderate",
        @Query("order") order: String = "relevance",
        @Query("publishedAfter") publishedAfter: String? = null
    ): Response<YouTubeSearchResponse>
}

class VideoService {
    private val api: YouTubeApi

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(YouTubeApi::class.java)
    }

    suspend fun searchVideos(
        query: String,
        maxResults: Int = 10,
        apiKey: String,
        videoDuration: String? = null,
        relevanceLanguage: String = "en",
        safeSearch: String = "moderate",
        order: String = "relevance",
        publishedAfter: String? = null
    ): Response<YouTubeSearchResponse> {
        return api.searchVideos(
            query = query,
            maxResults = maxResults,
            apiKey = apiKey,
            videoDuration = videoDuration,
            relevanceLanguage = relevanceLanguage,
            safeSearch = safeSearch,
            order = order,
            publishedAfter = publishedAfter
        )
    }

    companion object {
        @Volatile
        private var instance: VideoService? = null

        fun getInstance(): VideoService {
            return instance ?: synchronized(this) {
                instance ?: VideoService().also { instance = it }
            }
        }
    }
}