package com.rajkumar.cheerly.Podcast

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TeddyPodcastRepository private constructor() {
    private val TAG = "TeddyPodcastRepo"
    private val API_KEY = "b09988f38897940d7023fc73c66f87eb3021306ef9b232c6522bfb120202040d0d726ec3d943779b2f19dde94b0637cd63"
    private val USER_ID = "2031"
    private val BASE_URL = "https://api.taddy.org/graphql"  // Added /graphql endpoint
    private val CONTENT_TYPE = "application/json"

    data class PodcastMoodCriteria(
        val searchTerms: String,
        val alternateTerms: String,
        val topicFocus: List<String>,
        val contentType: String,
        val maxDuration: Int = 3600
    )

    private val moodParameters: Map<String, PodcastMoodCriteria> = mapOf(
        "happy" to PodcastMoodCriteria(
            searchTerms = "positive psychology growth mindset",
            alternateTerms = "success stories inspiration",
            topicFocus = listOf("personal development", "positive mindset", "achievement"),
            contentType = "uplifting stories and motivation",
            maxDuration = 2700
        ),
        "sad" to PodcastMoodCriteria(
            searchTerms = "emotional healing mindfulness therapy",
            alternateTerms = "self compassion mental wellness",
            topicFocus = listOf("emotional resilience", "coping strategies", "mental health"),
            contentType = "therapeutic content and gentle discussions",
            maxDuration = 3600
        ),
        "anxious" to PodcastMoodCriteria(
            searchTerms = "anxiety relief mindfulness meditation",
            alternateTerms = "stress management calming techniques",
            topicFocus = listOf("anxiety management", "breathing exercises", "present moment awareness"),
            contentType = "guided relaxation and expert advice",
            maxDuration = 1800
        ),
        "focused" to PodcastMoodCriteria(
            searchTerms = "deep work productivity flow state",
            alternateTerms = "concentration techniques mental clarity",
            topicFocus = listOf("cognitive enhancement", "productivity systems", "focus techniques"),
            contentType = "structured learning and practical techniques",
            maxDuration = 2400
        ),
        "bored" to PodcastMoodCriteria(
            searchTerms = "entertaining stories unusual facts",
            alternateTerms = "comedy interesting discussions",
            topicFocus = listOf("entertainment", "comedy", "engaging stories"),
            contentType = "entertaining and engaging content",
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
                val searchQuery = "${moodCriteria.searchTerms} ${moodCriteria.alternateTerms}"

                // Properly formatted GraphQL query with variables
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

                Log.d(TAG, "Request Body: ${queryJson}")

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Response: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("errors")) {
                        Log.e(TAG, "GraphQL errors: ${jsonResponse.getJSONArray("errors")}")
                        emptyList()
                    } else {
                        val podcastSeries = parsePodcastSeries(responseBody)
                        podcastSeries?.episodes?.let {
                            convertTeddyToPodcastEpisodes(it, podcastSeries).take(3)
                        } ?: emptyList()
                    }
                } else {
                    Log.e(TAG, "API call failed with code: ${response.code} and message: ${response.message}")
                    emptyList()
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









