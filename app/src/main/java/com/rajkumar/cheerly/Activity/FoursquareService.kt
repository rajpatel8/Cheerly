package com.rajkumar.cheerly.Activity

import com.rajkumar.cheerly.Activity.Models.FoursquareSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FoursquareService {
    @GET("v3/places/search")
    suspend fun searchVenues(
        @Header("Accept") accept: String = "application/json",
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("ll") latLong: String,
        @Query("radius") radius: Int,
        @Query("sort") sort: String = "RATING",
        @Query("limit") limit: Int = 20,
        @Query("categories") categories: String? = null
    ): Response<FoursquareSearchResponse>

    companion object {
        const val BASE_URL = "https://api.foursquare.com/"
    }
}