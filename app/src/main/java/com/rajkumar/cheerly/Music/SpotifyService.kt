package com.rajkumar.cheerly.Music

import retrofit2.Response
import retrofit2.http.*

interface SpotifyAuthService {
    @POST("api/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<SpotifyTokenResponse>

    @POST("api/token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<SpotifyTokenResponse>
}

interface SpotifyApiService {
    @GET("recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") auth: String,
        @Query("seed_tracks") seedTracks: String?,
        @Query("seed_genres") seedGenres: String,
        @Query("target_valence") targetValence: Float,
        @Query("target_energy") targetEnergy: Float,
        @Query("limit") limit: Int = 3,
        @Query("market") market: String = "US"
    ): Response<SpotifyRecommendationsResponse>

    @GET("me/top/tracks")
    suspend fun getTopTracks(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 5,
        @Query("time_range") timeRange: String = "medium_term"
    ): Response<TopTracksResponse>

    @GET("me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 10
    ): Response<RecentlyPlayedResponse>
}


// Response Data Classes
data class SpotifyTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String? = null,
    val scope: String? = null
)

//data class TopTracksResponse(
//    val items: List<Track>
//)

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

data class TopTracksResponse(
    val items: List<Track>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class TopArtistsResponse(
    val items: List<Artist>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class RecentlyPlayedResponse(
    val items: List<PlayHistoryObject>
)

data class SavedTracksResponse(
    val items: List<SavedTrack>,
    val total: Int,
    val limit: Int,
    val offset: Int
)


data class PlayHistoryObject(
    val track: Track,
    val played_at: String
)

data class SavedTrack(
    val added_at: String,
    val track: Track
)

data class Cursors(
    val after: String?,
    val before: String?
)

data class Context(
    val type: String,
    val href: String,
    val external_urls: ExternalUrls,
    val uri: String
)