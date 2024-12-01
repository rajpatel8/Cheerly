package com.rajkumar.cheerly.Podcast

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PodcastService {
    @GET("api/v2/search")
    suspend fun searchPodcasts(
        @Header("X-ListenAPI-Key") apiKey: String,
        @Query("q") query: String,
        @Query("type") type: String = "episode",
        @Query("language") language: String = "English",
        @Query("len_min") lenMin: Int = 0,
        @Query("len_max") lenMax: Int = 120,
        @Query("genre_ids") genre: String? = null,
        @Query("sort_by_date") sortByDate: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PodcastResponse>
}