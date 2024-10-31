package com.rajkumar.cheerly

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SpotifyAuthService {
    @POST("api/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<SpotifyTokenResponse>
}

interface SpotifyApiService {
    @GET("recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 20,
        @Query("market") market: String = "US",
        @Query("seed_genres") seedGenres: String,
        @Query("target_valence") targetValence: Float,
        @Query("target_energy") targetEnergy: Float
    ): Response<SpotifyRecommendationsResponse>

    @GET("recommendations/available-genre-seeds")
    suspend fun getAvailableGenres(
        @Header("Authorization") auth: String
    ): Response<GenreSeedsResponse>
}

// Response Data Classes
data class SpotifyTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

data class SpotifyRecommendationsResponse(
    val tracks: List<Track>,
    val seeds: List<RecommendationSeed>
)

data class RecommendationSeed(
    val id: String,
    val type: String,
    val href: String
)

data class GenreSeedsResponse(
    val genres: List<String>
)