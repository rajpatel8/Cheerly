package com.rajkumar.cheerly.Podcast

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PodcastRepository {
    private val API_KEY = "e3bdb4b10c1142eb99de1b3ef03213c0"  // Replace with actual key
    private val BASE_URL = "https://listen-api.listennotes.com/"

    private val podcastService: PodcastService

    // Define mood-specific parameters
    data class MoodParameters(
        val queries: List<String>,
        val genres: List<String>,
        val minLength: Int = 0,          // in minutes
        val maxLength: Int = 120,        // in minutes
        val excludeKeywords: List<String> = emptyList(),
        val includeKeywords: List<String> = emptyList(),
        val sortBy: String = "relevance" // relevance, recent, or popular
    )

    private val moodParameters = mapOf(
        "happy" to MoodParameters(
            queries = listOf(
                "positive mindset",
                "happiness habits",
                "joy meditation",
                "uplifting stories",
                "gratitude practice"
            ),
            genres = listOf("self-improvement", "health", "spirituality"),
            minLength = 10,
            maxLength = 45,
            includeKeywords = listOf("positive", "happiness", "joy", "gratitude", "inspiration"),
            excludeKeywords = listOf("anxiety", "depression", "stress"),
            sortBy = "popular"
        ),
        "sad" to MoodParameters(
            queries = listOf(
                "emotional healing",
                "mental wellness",
                "coping strategies",
                "mindfulness meditation",
                "self-care practice"
            ),
            genres = listOf("mental-health", "meditation", "self-help"),
            minLength = 20,
            maxLength = 60,
            includeKeywords = listOf("healing", "comfort", "hope", "mindfulness", "peace"),
            excludeKeywords = listOf("tragedy", "crisis", "trauma"),
            sortBy = "relevance"
        ),
        "excited" to MoodParameters(
            queries = listOf(
                "motivation boost",
                "success stories",
                "high energy",
                "workout motivation",
                "achievement mindset"
            ),
            genres = listOf("motivation", "business", "sports"),
            minLength = 15,
            maxLength = 45,
            includeKeywords = listOf("motivation", "success", "energy", "achievement", "power"),
            excludeKeywords = listOf("relaxation", "sleep", "meditation"),
            sortBy = "recent"
        ),
        "relaxed" to MoodParameters(
            queries = listOf(
                "sleep meditation",
                "calming sounds",
                "peaceful stories",
                "nature sounds",
                "gentle meditation"
            ),
            genres = listOf("meditation", "wellness", "spirituality"),
            minLength = 30,
            maxLength = 90,
            includeKeywords = listOf("relaxation", "calm", "peace", "meditation", "mindfulness"),
            excludeKeywords = listOf("motivation", "workout", "high-energy"),
            sortBy = "relevance"
        )
    )

    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        podcastService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PodcastService::class.java)
    }

    suspend fun getPodcastRecommendations(mood: String): List<PodcastEpisode> {
        return withContext(Dispatchers.IO) {
            try {
                val parameters = moodParameters[mood.lowercase()] ?: moodParameters["happy"]!!
                val allEpisodes = mutableListOf<PodcastEpisode>()

                // Try different queries from the mood parameters
                for (query in parameters.queries.shuffled().take(3)) {
                    try {
                        val response = podcastService.searchPodcasts(
                            apiKey = API_KEY,
                            query = query,
                            type = "episode",
                            language = "English",
                            lenMin = parameters.minLength,
                            lenMax = parameters.maxLength,
                            genre = parameters.genres.joinToString(","),
                            sortByDate = if (parameters.sortBy == "recent") 1 else 0,
                            size = 10
                        )

                        if (response.isSuccessful) {
                            val episodes = response.body()?.results ?: emptyList()
                            allEpisodes.addAll(filterEpisodes(episodes, parameters))
                        }
                    } catch (e: Exception) {
                        Log.e("PodcastRepository", "Error fetching podcasts for query: $query", e)
                        continue
                    }
                }

                // Process and return the final list
                allEpisodes
                    .distinctBy { it.id }
                    .shuffled()
                    .take(3)
                    .also { episodes ->
                        Log.d("PodcastRepository", "Returning ${episodes.size} episodes for mood: $mood")
                    }
            } catch (e: Exception) {
                Log.e("PodcastRepository", "Error in getPodcastRecommendations", e)
                emptyList()
            }
        }
    }

    private fun filterEpisodes(
        episodes: List<PodcastEpisode>,
        parameters: MoodParameters
    ): List<PodcastEpisode> {
        return episodes.filter { episode ->
            val title = episode.title_original.lowercase()
            val description = episode.description_original.lowercase()
            val content = "$title $description"

            // Check for explicit content
            if (episode.explicit_content) {
                return@filter false
            }

            // Check for included keywords
            val hasIncludedKeywords = parameters.includeKeywords.isEmpty() ||
                    parameters.includeKeywords.any { keyword ->
                        content.contains(keyword.lowercase())
                    }

            // Check for excluded keywords
            val hasExcludedKeywords = parameters.excludeKeywords.any { keyword ->
                content.contains(keyword.lowercase())
            }

            // Final filtering decision
            hasIncludedKeywords && !hasExcludedKeywords
        }
    }

    companion object {
        @Volatile
        private var instance: PodcastRepository? = null

        fun getInstance(): PodcastRepository {
            return instance ?: synchronized(this) {
                instance ?: PodcastRepository().also { instance = it }
            }
        }
    }
}