package com.rajkumar.cheerly.Activity

import com.rajkumar.cheerly.Activity.Models.FoursquareSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FoursquareService {
    @GET("v3/places/search")
    suspend fun searchVenues(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("ll") latLong: String,
        @Query("radius") radius: Int,
        @Query("sort") sort: String = "RATING",
        @Query("limit") limit: Int = 10,
        @Query("categories") categories: String? = null,
        @Query("open_now") openNow: Boolean = true,
        @Query("fields") fields: String = "fsq_id,name,categories,location,distance,rating,photos"
    ): Response<FoursquareSearchResponse>

    companion object {
        const val BASE_URL = "https://api.foursquare.com/"
        const val DEFAULT_FIELDS = "fsq_id,name,categories,location,distance,rating,photos"
        const val DEFAULT_SORT = "RATING"
        const val DEFAULT_LIMIT = 10
    }
}