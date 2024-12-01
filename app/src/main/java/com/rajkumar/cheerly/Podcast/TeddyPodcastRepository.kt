package com.rajkumar.cheerly.Podcast

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TeddyPodcastRepository private constructor() {
    private val TAG = "TeddyPodcastRepo"
    private val API_KEY = "b09988f38897940d7023fc73c66f87eb3021306ef9b232c6522bfb120202040d0d726ec3d943779b2f19dde94b0637cd63"
    //    New API Key
    //    private val API_KEY = "a758031b2b236b3d39a0bb3aa1dfd37e1e97f91ba54e83424279810ab1a79496866cbd775b4db7f65348ccf9ef574620b0"
    private val USER_ID = "2031"
    private val BASE_URL = "https://api.taddy.org/graphql"
    private val CONTENT_TYPE = "application/json"

    data class PodcastMoodCriteria(
        val searchTerms: String,
        val genre: String,
        val topicFocus: List<String>,
        val maxDuration: Int = 3600
    )

    private val moodParameters: Map<String, PodcastMoodCriteria> = mapOf(
        "happy" to PodcastMoodCriteria(
            searchTerms = "positive psychology happiness motivation comedy",
            genre = "Self-Help, Comedy",
            topicFocus = listOf("happiness", "motivation", "comedy", "success"),
            maxDuration = 2400
        ),
        "sad" to PodcastMoodCriteria(
            searchTerms = "mindfulness healing emotional wellness meditation",
            genre = "Mental Health, Wellness",
            topicFocus = listOf("healing", "mindfulness", "support", "meditation"),
            maxDuration = 3000
        ),
        "excited" to PodcastMoodCriteria(
            searchTerms = "high energy motivation sports adventure",
            genre = "Sports, Adventure",
            topicFocus = listOf("adventure", "sports", "fitness", "challenge"),
            maxDuration = 1800
        ),
        "relaxed" to PodcastMoodCriteria(
            searchTerms = "meditation calm relaxation mindfulness nature",
            genre = "Meditation, Nature",
            topicFocus = listOf("meditation", "relaxation", "mindfulness", "peace"),
            maxDuration = 2700
        ),
        "bored" to PodcastMoodCriteria(
            searchTerms = "interesting stories mystery entertainment fascinating",
            genre = "Entertainment, Storytelling",
            topicFocus = listOf("mystery", "stories", "entertainment", "comedy"),
            maxDuration = 2400
        ),
        "anxious" to PodcastMoodCriteria(
            searchTerms = "anxiety relief stress management calming meditation",
            genre = "Mental Health, Wellness",
            topicFocus = listOf("anxiety", "stress relief", "calming", "mindfulness"),
            maxDuration = 1800
        ),
        "focused" to PodcastMoodCriteria(
            searchTerms = "productivity focus concentration learning",
            genre = "Education, Business",
            topicFocus = listOf("productivity", "learning", "focus", "efficiency"),
            maxDuration = 2400
        )
    )

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getPodcastRecommendations(mood: String): List<PodcastEpisode> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting podcast fetch for mood: $mood")

                val moodCriteria = moodParameters[mood.lowercase()] ?: moodParameters["happy"]!!

                // Create multiple search queries for variety
                val searchQueries = listOf(
                    moodCriteria.searchTerms,
                    "${moodCriteria.genre} ${moodCriteria.topicFocus.random()}",
                    moodCriteria.topicFocus.joinToString(" ").take(2)
                )

                val allEpisodes = mutableListOf<PodcastEpisode>()

                // Try each search query
                for (searchQuery in searchQueries) {
                    val queryJson = JSONObject().apply {
                        put("query", """
                            query GetPodcastsByName(${"$"}name: String!) {
                                getPodcastSeries(name: ${"$"}name) {
                                    uuid
                                    name
                                    itunesId
                                    description
                                    imageUrl
                                    itunesInfo {
                                        uuid
                                        publisherName
                                        baseArtworkUrlOf(size: 640)
                                    }
                                    episodes {
                                        uuid
                                        name
                                        description
                                        audioUrl
                                    }
                                }
                            }
                        """.trimIndent())
                        put("variables", JSONObject().put("name", searchQuery))
                    }

                    val requestBody = queryJson.toString().toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url(BASE_URL)
                        .post(requestBody)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-USER-ID", USER_ID)
                        .addHeader("X-API-KEY", API_KEY)
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && responseBody != null) {
                        val podcastSeries = parsePodcastSeries(responseBody)
                        podcastSeries?.episodes?.let { episodes ->
                            allEpisodes.addAll(convertTeddyToPodcastEpisodes(episodes, podcastSeries))
                        }
                    }
                }

                // Filter and return unique episodes
                allEpisodes
                    .distinctBy { "${it.title_original}${it.podcast.publisher_original}" }
                    .shuffled() // Add randomness to prevent same order
                    .take(7)
                    .also {
                        Log.d(TAG, "Returning ${it.size} unique episodes for mood: $mood")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching podcasts: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun parsePodcastSeries(responseData: String): PodcastSeries? {
        return try {
            val jsonObject = JSONObject(responseData)
            val data = jsonObject.optJSONObject("data")
            val getPodcastSeries = data?.optJSONObject("getPodcastSeries")

            if (getPodcastSeries == null) {
                Log.d(TAG, "No podcast series found in response")
                return null
            }

            val itunesInfoObject = getPodcastSeries.optJSONObject("itunesInfo")
            val itunesInfo = itunesInfoObject?.let {
                ItunesInfo(
                    uuid = it.optString("uuid"),
                    publisherName = it.optString("publisherName"),
                    baseArtworkUrlOf = it.optString("baseArtworkUrlOf")
                )
            }

            val episodesArray = getPodcastSeries.optJSONArray("episodes")
            val episodes = mutableListOf<TeddyEpisode>()

            if (episodesArray != null) {
                for (i in 0 until episodesArray.length()) {
                    val episode = episodesArray.getJSONObject(i)
                    episodes.add(
                        TeddyEpisode(
                            uuid = episode.optString("uuid"),
                            name = episode.optString("name"),
                            description = episode.optString("description"),
                            audioUrl = episode.optString("audioUrl")
                        )
                    )
                }
            }

            PodcastSeries(
                uuid = getPodcastSeries.optString("uuid"),
                name = getPodcastSeries.optString("name"),
                itunesId = getPodcastSeries.optString("itunesId"),
                description = getPodcastSeries.optString("description"),
                imageUrl = getPodcastSeries.optString("imageUrl"),
                itunesInfo = itunesInfo,
                episodes = episodes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing podcast series JSON: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun convertTeddyToPodcastEpisodes(
        teddyEpisodes: List<TeddyEpisode>,
        podcastSeries: PodcastSeries
    ): List<PodcastEpisode> {
        return teddyEpisodes.map {
            PodcastEpisode(
                id = it.uuid,
                title_original = it.name,
                description_original = it.description,
                thumbnail = podcastSeries.imageUrl,
                audio = it.audioUrl,
                audio_length_sec = 0, // Not provided in the API
                explicit_content = false, // Not provided in the API
                link = "", // Not provided in the API
                podcast = PodcastInfo(
                    id = podcastSeries.uuid,
                    title_original = podcastSeries.name,
                    publisher_original = podcastSeries.itunesInfo?.publisherName ?: "Unknown Publisher",
                    thumbnail = podcastSeries.itunesInfo?.baseArtworkUrlOf ?: ""
                )
            )
        }
    }

    companion object {
        @Volatile
        private var instance: TeddyPodcastRepository? = null

        fun getInstance(): TeddyPodcastRepository {
            return instance ?: synchronized(this) {
                instance ?: TeddyPodcastRepository().also { instance = it }
            }
        }
    }
}









