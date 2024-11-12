package com.rajkumar.cheerly.Activity

import com.rajkumar.cheerly.Activity.Models.EventResponse
import com.rajkumar.cheerly.Activity.Models.PlaceResponse
import com.rajkumar.cheerly.Activity.Models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenStreetMapService {
    @GET("api/0.6/map")
    suspend fun searchNearbyPlaces(
        @Query("bbox") bbox: String
    ): Response<PlaceResponse>
}

interface TicketmasterService {
    @GET("discovery/v2/events")
    suspend fun searchEvents(
        @Query("apikey") apiKey: String,
        @Query("latlong") latLong: String,
        @Query("radius") radius: String,
        @Query("unit") unit: String = "km",
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "date,asc"
    ): Response<EventResponse>
}

interface OpenWeatherService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
}