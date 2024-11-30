package com.rajkumar.cheerly.Video

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository private constructor() {
    private val videoService = VideoService.getInstance()
    private val TAG = "VideoRepository"
    private val API_KEY = "AIzaSyAKookqX3kZziARW8rTPNSk1SxGmS4iLtM"

    private val moodChannels = mapOf(
        "happy" to listOf(
            "The Ellen Show",
            "The Tonight Show Starring Jimmy Fallon",
            "Good Mythical Morning",
            "BuzzFeed Video",
            "The Late Late Show with James Corden",
            "Food Network",
            "Animal Planet",
            "Disney",
            "Just For Laughs Gags",
            "America's Got Talent"
        ),
        "sad" to listOf(
            // Feel-Good/Inspiring Stories
            "The Dodo",
            "Goalcast",
            "Jay Shetty",
            "SoulPancake",
            "Nas Daily",

            // Wholesome Entertainment
            "Good Mythical Morning",
            "Yes Theory",
            "Daily Dose Of Internet",
            "Some Good News",
            "Cut",

            // Motivational Channels
            "Prince Ea",
            "Absolute Motivation",
            "Tom Bilyeu",
            "Lewis Howes",
            "Improvement Pill",

            // Positive Lifestyle
            "Binging with Babish",
            "Matt D'Avella",
            "Peter McKinnon",
            "Casey Neistat",
            "Peaceful Cuisine"
        ),
        "bored" to listOf(
            "Veritasium",
            "Mark Rober",
            "SmarterEveryDay",
            "Steve Mould",
            "ScienceC",
            "Vsauce",
            "Tom Scott",
            "Wendover Productions",
            "Kurzgesagt",
            "Real Engineering",
            "The Action Lab",
            "Minute Physics",
            "Mind Your Decisions",
            "3Blue1Brown",
            "AsapSCIENCE"
        ),
        "anxious" to listOf(
            "Headspace",
            "Calm",
            "Yoga With Adriene",
            "TheHonestGuys",
            "Nature Soundscapes",
            "Peaceful Cuisine",
            "ASMRMagic",
            "Walking Videos",
            "Relax With Nature",
            "Calmed By Nature"
        ),
        "focused" to listOf(
            "Study MD",
            "College Music",
            "ChilledCow",
            "StudyMusic",
            "Productivity Game",
            "The Jazz Hop Café",
            "Merve",
            "Focus@Will",
            "Brain Beats",
            "Study Together"
        ),
        "excited" to listOf(
            "Red Bull",
            "Dude Perfect",
            "Marshmello",
            "Red Bull Motorsports",
            "GoPro",
            "X Games",
            "People Are Awesome",
            "Nitro Circus",
            "Monster Energy",
            "Thrill Zone",
            "Adrenaline Addiction",
            "Epic Adventures",
            "Extreme Sports Channel",
            "STORROR",
            "Team Edge"
        )
    )

    suspend fun getVideoRecommendations(mood: String): List<Video> = withContext(Dispatchers.IO) {
        try {
            val channels = moodChannels[mood.lowercase()] ?: moodChannels["happy"]!!
            val allVideos = mutableListOf<Video>()

            // Select 5 random channels
            val selectedChannels = channels.shuffled().take(5)
            Log.d(TAG, "Selected channels for $mood: $selectedChannels")

            // Get one video from each channel
            selectedChannels.forEach { selectedChannel ->
                try {
                    val response = videoService.searchVideos(
                        query = selectedChannel,
                        maxResults = 5,
                        apiKey = API_KEY,
                        order = "relevance",
                        videoDuration = "medium",
                        relevanceLanguage = "en"
                    )

                    if (response.isSuccessful) {
                        response.body()?.items?.firstOrNull { video ->
                            videoMatchesChannel(video, selectedChannel)
                        }?.let { video ->
                            allVideos.add(
                                Video(
                                    id = video.id.videoId,
                                    title = sanitizeTitle(video.snippet.title),
                                    channelName = video.snippet.channelTitle,
                                    thumbnailUrl = getBestThumbnail(video.snippet.thumbnails),
                                    videoUrl = "https://www.youtube.com/watch?v=${video.id.videoId}"
                                )
                            )
                            Log.d(TAG, "Added video from channel: ${video.snippet.channelTitle}")
                        }
                    } else {
                        Log.e(TAG, "API error for $selectedChannel: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching from channel: $selectedChannel", e)
                }
            }

            return@withContext allVideos.also { results ->
                Log.d(TAG, "Final recommendations for $mood: ${results.size} videos")
                results.forEach { video ->
                    Log.d(TAG, "Recommending: ${video.title} by ${video.channelName}")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations", e)
            emptyList()
        }
    }

    private fun videoMatchesChannel(video: YouTubeVideo, channelQuery: String): Boolean {
        val title = video.snippet.title.lowercase()
        val channelTitle = video.snippet.channelTitle.lowercase()
        val query = channelQuery.lowercase()

        // Split query into words
        val queryWords = query.split(" ")

        // Check if most of the query words appear in either title or channel name
        val matchCount = queryWords.count { word ->
            title.contains(word) || channelTitle.contains(word)
        }

        return matchCount >= queryWords.size / 2
    }

    private fun sanitizeTitle(title: String): String {
        return title.replace(Regex("&#39;"), "'")
            .replace(Regex("&quot;"), "\"")
            .replace(Regex("&amp;"), "&")
    }

    private fun getBestThumbnail(thumbnails: Thumbnails): String {
        return thumbnails.high?.url
            ?: thumbnails.medium?.url
            ?: thumbnails.default.url
    }

    companion object {
        @Volatile
        private var instance: VideoRepository? = null

        fun getInstance(requireContext: Context): VideoRepository {
            return instance ?: synchronized(this) {
                instance ?: VideoRepository().also { instance = it }
            }
        }
    }
}