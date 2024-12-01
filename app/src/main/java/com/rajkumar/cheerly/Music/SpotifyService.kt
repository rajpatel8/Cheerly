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
    @GET("me/top/tracks")
    suspend fun getTopTracks(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 50,
        @Query("time_range") timeRange: String = "medium_term"
    ): Response<TopTracksResponse>

    @GET("me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 50
    ): Response<RecentlyPlayedResponse>

    @GET("artists/{id}")
    suspend fun getArtist(
        @Header("Authorization") auth: String,
        @Path("id") artistId: String
    ): Response<Artist>

    @POST("users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") auth: String,
        @Path("user_id") userId: String,
        @Body playlistRequest: CreatePlaylistRequest
    ): Response<PlaylistResponse>

    @POST("playlists/{playlist_id}/tracks")
    suspend fun addTracksToPlaylist(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlistId: String,
        @Body tracksRequest: AddTracksRequest
    ): Response<AddTracksResponse>

    @GET("me")
    suspend fun getCurrentUser(
        @Header("Authorization") auth: String
    ): Response<UserProfile>

    @GET("me/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int = 50
    ): Response<PlaylistsResponse>

    @GET("playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlistId: String
    ): Response<PlaylistTracksResponse>
}

// Response Data Classes
data class SpotifyTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String? = null,
    val scope: String? = null
)

data class TopTracksResponse(
    val items: List<Track>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class RecentlyPlayedResponse(
    val items: List<PlayHistoryObject>
)

data class PlayHistoryObject(
    val track: Track,
    val played_at: String
)