package com.rajkumar.cheerly.Music

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import net.openid.appauth.AuthState

class SpotifyRepository(private val context: Context) {
    private val TAG = "SpotifyRepository"
    private val BASE_API_URL = "https://api.spotify.com/v1/"
    private val PREFS_NAME = "SpotifyAuthPrefs"
    private val KEY_AUTH_STATE = "auth_state"

    private val apiService: SpotifyApiService

    private val moodKeywords = mapOf(
        "happy" to listOf(
            "dance", "pop", "happy", "joy", "fun", "party", "sunshine", "summer",
            "bright", "upbeat", "groove", "celebration"
        ),
        "sad" to listOf(
            "acoustic", "piano", "melancholy", "slow", "ballad", "sad",
            "heartbreak", "emotional", "quiet"
        ),
        "excited" to listOf(
            "dance", "edm", "party", "energy", "power", "rock", "beat",
            "rhythm", "electro", "fast", "club"
        ),
        "relaxed" to listOf(
            "chill", "ambient", "calm", "peace", "soft", "gentle", "meditation",
            "relax", "smooth", "easy"
        ),
        "bored" to listOf(
            "discover", "new", "fresh", "unique", "different", "interesting",
            "exciting", "dynamic", "variety"
        ),
        "anxious" to listOf(
            "calm", "meditation", "peaceful", "quiet", "gentle", "soft",
            "soothing", "instrumental", "ambient"
        ),
        "focused" to listOf(
            "instrumental", "study", "focus", "concentration", "classical",
            "ambient", "minimal", "piano"
        )
    )

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApiService::class.java)
    }

    private fun getStoredAuthToken(): String? {
        Log.d(TAG, "Retrieving stored auth token")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val authStateJson = prefs.getString(KEY_AUTH_STATE, null)
        return if (authStateJson != null) {
            try {
                val authState = AuthState.jsonDeserialize(authStateJson)
                Log.d(TAG, "Auth token retrieved successfully")
                authState.accessToken
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing auth state", e)
                null
            }
        } else {
            Log.w(TAG, "No auth state found in preferences")
            null
        }
    }

    suspend fun createOrUpdatePlaylist(mood: String, tracks: List<Track>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = getStoredAuthToken() ?: return@withContext Result.failure(Exception("Not authenticated"))
            val auth = "Bearer $token"
            val playlistName = "Cheerly-${mood.capitalize()}"

            // First, check if playlist already exists
            val existingPlaylist = findExistingPlaylist(auth, playlistName)

            if (existingPlaylist != null) {
                // Get existing tracks to avoid duplicates
                val existingTracks = getPlaylistTracks(auth, existingPlaylist.id)
                val existingTrackIds = existingTracks.map { it.id }.toSet()

                // Filter out tracks that are already in the playlist
                val newTracks = tracks.filterNot { existingTrackIds.contains(it.id) }

                if (newTracks.isNotEmpty()) {
                    // Add new tracks to existing playlist
                    val trackUris = newTracks.map { "spotify:track:${it.id}" }
                    val addTracksResponse = apiService.addTracksToPlaylist(
                        auth = auth,
                        playlistId = existingPlaylist.id,
                        tracksRequest = AddTracksRequest(trackUris)
                    )

                    return@withContext if (addTracksResponse.isSuccessful) {
                        Result.success("existing")
                    } else {
                        Result.failure(Exception("Failed to add tracks to existing playlist"))
                    }
                } else {
                    return@withContext Result.success("no_new_tracks")
                }
            } else {
                // Create new playlist if it doesn't exist
                val userResponse = apiService.getCurrentUser(auth)
                if (!userResponse.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to get user profile"))
                }
                val userId = userResponse.body()?.id ?: return@withContext Result.failure(Exception("User ID not found"))

                // Create new playlist
                val description = "Music recommendations for $mood mood by Cheerly"
                val createPlaylistResponse = apiService.createPlaylist(
                    auth = auth,
                    userId = userId,
                    playlistRequest = CreatePlaylistRequest(
                        name = playlistName,
                        description = description
                    )
                )

                if (!createPlaylistResponse.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to create playlist"))
                }

                val playlistId = createPlaylistResponse.body()?.id
                    ?: return@withContext Result.failure(Exception("Playlist ID not found"))

                // Add tracks to new playlist
                val trackUris = tracks.map { "spotify:track:${it.id}" }
                val addTracksResponse = apiService.addTracksToPlaylist(
                    auth = auth,
                    playlistId = playlistId,
                    tracksRequest = AddTracksRequest(trackUris)
                )

                return@withContext if (addTracksResponse.isSuccessful) {
                    Result.success("new")
                } else {
                    Result.failure(Exception("Failed to add tracks to playlist"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error managing playlist", e)
            Result.failure(e)
        }
    }

    private suspend fun findExistingPlaylist(auth: String, playlistName: String): PlaylistItem? {
        try {
            val response = apiService.getUserPlaylists(auth)
            if (response.isSuccessful) {
                return response.body()?.items?.find { it.name == playlistName }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding existing playlist", e)
        }
        return null
    }

    private suspend fun getPlaylistTracks(auth: String, playlistId: String): List<Track> {
        try {
            val response = apiService.getPlaylistTracks(auth, playlistId)
            if (response.isSuccessful) {
                return response.body()?.items?.map { it.track } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playlist tracks", e)
        }
        return emptyList()
    }

    suspend fun getUserTopTracks(): List<Track> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching user's top tracks")
            val token = getStoredAuthToken() ?: return@withContext emptyList()

            val response = apiService.getTopTracks(
                auth = "Bearer $token",
                limit = 50,
                timeRange = "long_term"
            )

            if (response.isSuccessful) {
                val tracks = response.body()?.items ?: emptyList()
                Log.d(TAG, "Successfully fetched ${tracks.size} top tracks")
                tracks
            } else {
                Log.e(TAG, "Failed to get top tracks: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching top tracks", e)
            emptyList()
        }
    }

    suspend fun getUserRecentTracks(): List<Track> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching user's recent tracks")
            val token = getStoredAuthToken() ?: return@withContext emptyList()

            val response = apiService.getRecentlyPlayed(
                auth = "Bearer $token",
                limit = 50
            )

            if (response.isSuccessful) {
                val tracks = response.body()?.items?.map { it.track } ?: emptyList()
                Log.d(TAG, "Successfully fetched ${tracks.size} recent tracks")
                tracks
            } else {
                Log.e(TAG, "Failed to get recent tracks: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent tracks", e)
            emptyList()
        }
    }

    suspend fun getRecommendations(mood: String): List<Track> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting recommendations process for mood: $mood")

            // Get user preferences
            val userPreferences = getUserPreferences()
            Log.d(TAG, "User preferences: $userPreferences")

            // Get a larger pool of tracks
            val allTracks = mutableListOf<Track>()
            allTracks.addAll(getUserTopTracks())
            allTracks.addAll(getUserRecentTracks())

            // Remove duplicates
            val uniqueTracks = allTracks.distinctBy { it.id }.toMutableList()
            Log.d(TAG, "Total unique tracks available: ${uniqueTracks.size}")

            val recommendations = mutableListOf<Track>()
            val processedArtists = mutableSetOf<String>()

            // First, add tracks matching user preferences
            for (preference in userPreferences) {
                val matchingTracks = uniqueTracks.filter { track ->
                    val matchesPreference = matchesPreference(track, preference)
                    val isNewArtist = track.artists.none { it.id in processedArtists }
                    matchesPreference && isNewArtist
                }.take(2)

                recommendations.addAll(matchingTracks)
                uniqueTracks.removeAll(matchingTracks.toSet())
                processedArtists.addAll(matchingTracks.flatMap { it.artists.map { artist -> artist.id } })
            }

            // Then, add mood-based tracks
            val moodKeywordList = moodKeywords[mood.lowercase()] ?: moodKeywords["happy"]!!
            val moodBasedTracks = uniqueTracks
                .asSequence()
                .filter { track ->
                    track.artists.none { it.id in processedArtists } &&
                            calculateMoodScore(track, moodKeywordList) > 0.0
                }
                .sortedByDescending { calculateMoodScore(it, moodKeywordList) }
                .take(5 - recommendations.size)
                .toList()

            recommendations.addAll(moodBasedTracks)

            // If we still need more tracks, add random ones ensuring artist diversity
            if (recommendations.size < 5) {
                val remainingTracks = uniqueTracks
                    .filter { track -> track.artists.none { it.id in processedArtists } }
                    .shuffled()
                    .take(5 - recommendations.size)

                recommendations.addAll(remainingTracks)
            }

            Log.d(TAG, "Final recommendations (${recommendations.size} tracks):")
            recommendations.forEach { track ->
                Log.d(TAG, "Track: ${track.name} by ${track.artists.joinToString { it.name }}")
            }

            recommendations.shuffled()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendations", e)
            emptyList()
        }
    }

    private fun getUserPreferences(): Set<String> {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getStringSet("selectedMusicOptions", emptySet()) ?: emptySet()
    }

    private fun matchesPreference(track: Track, preference: String): Boolean {
        val searchTerms = when (preference.lowercase()) {
            "rock" -> listOf("rock", "alternative", "metal", "indie")
            "jazz" -> listOf("jazz", "blues", "swing", "bebop")
            "classical" -> listOf("classical", "orchestra", "symphony", "piano")
            "hip-hop" -> listOf("hip hop", "rap", "hip-hop", "trap")
            "country" -> listOf("country", "folk", "bluegrass")
            else -> listOf(preference.lowercase())
        }

        return searchTerms.any { term ->
            track.name.lowercase().contains(term) ||
                    track.artists.any { it.name.lowercase().contains(term) } ||
                    track.album.name.lowercase().contains(term)
        }
    }

    private fun calculateMoodScore(track: Track, keywords: List<String>): Double {
        var score = 0.0

        // Check track name (highest weight)
        keywords.forEach { keyword ->
            if (track.name.lowercase().contains(keyword.lowercase())) {
                score += 1.0
            }
        }

        // Check artist names (medium weight)
        track.artists.forEach { artist ->
            keywords.forEach { keyword ->
                if (artist.name.lowercase().contains(keyword.lowercase())) {
                    score += 0.5
                }
            }
        }

        // Check album name (lower weight)
        keywords.forEach { keyword ->
            if (track.album.name.lowercase().contains(keyword.lowercase())) {
                score += 0.3
            }
        }

        return score
    }

    companion object {
        private const val TAG = "SpotifyRepository"
        @Volatile
        private var instance: SpotifyRepository? = null

        fun getInstance(context: Context): SpotifyRepository {
            return instance ?: synchronized(this) {
                Log.d(TAG, "Creating new SpotifyRepository instance")
                instance ?: SpotifyRepository(context).also { instance = it }
            }
        }
    }
}