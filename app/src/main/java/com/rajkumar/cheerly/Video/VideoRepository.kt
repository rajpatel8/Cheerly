package com.rajkumar.cheerly.Video

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepository private constructor() {
    private val videoService = VideoService.getInstance()
    private val TAG = "VideoRepository"
    private val API_KEY = "AIzaSyCssC4IkFKwOq7Jr0o3odM0sNHS4GFjjGM"

    private val moodChannels = mapOf(
        "happy" to listOf(
            "Michael Sealey Meditation",
            "Great Meditation",
            "The Honest Guys Meditations",
            "Yoga With Adriene",
            "New Horizon Meditation & Sleep Stories",
            "PowerThoughts Meditation Club",
            "Goodful",
            "Calm",
            "Headspace",
            "Positive Energy Meditation",
            "Daily Motivation",
            "Success Chasers",
            "Mind Valley"
        ),
        "sad" to listOf(
            "Michael Sealey Meditation",
            "Great Meditation",
            "The Honest Guys Meditations",
            "Yoga With Adriene",
            "New Horizon Meditation & Sleep Stories",
            "Jason Stephenson - Sleep Meditation Music",
            "Yellow Brick Cinema",
            "Calm",
            "Soothing Relaxation",
            "Nu Meditation Music",
            "Meditation Vacation",
            "Ambient Relaxation",
            "Meditation and Healing"
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
            "Michael Sealey Meditation",
            "Great Meditation",
            "The Honest Guys Meditations",
            "Yoga With Adriene",
            "New Horizon Meditation & Sleep Stories",
            "Calm",
            "Headspace",
            "Jason Stephenson",
            "The Mindful Movement",
            "Meditation Vacation",
            "Goodful",
            "Declutter The Mind",
            "Mindful Peace",
            "Daily Calm",
            "Zen Meditation Planet"
        ),
        "focused" to listOf(
            "sirgog",
            "Study MD",
            "ChilledCow",
            "The Jazz Hop Café",
            "College Music",
            "Relaxing White Noise",
            "Focus Music",
            "Study Music Project",
            "Quiet Quest - Study Music",
            "4K Study",
            "Productivity Game",
            "Focus at Will",
            "Brain Wave Power Music"
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

            Log.d(TAG, "Fetching videos for mood: $mood")

            // Get videos from each channel
            channels.forEach { channelName ->
                try {
                    // First, search for channel's content
                    val response = videoService.searchVideos(
                        query = "channel:\"$channelName\"",
                        maxResults = 5,
                        apiKey = API_KEY,
                        order = "date", // Get latest videos
                        videoDuration = "medium",
                        relevanceLanguage = "en"
                    )

                    if (response.isSuccessful) {
                        response.body()?.items?.mapNotNull { video ->
                            try {
                                // Only include videos from the exact channel name match
                                if (video.snippet.channelTitle.equals(channelName, ignoreCase = true)) {
                                    Video(
                                        id = video.id.videoId,
                                        title = sanitizeTitle(video.snippet.title),
                                        channelName = video.snippet.channelTitle,
                                        thumbnailUrl = getBestThumbnail(video.snippet.thumbnails),
                                        videoUrl = "https://www.youtube.com/watch?v=${video.id.videoId}"
                                    )
                                } else null
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing video from $channelName: ${e.message}")
                                null
                            }
                        }?.let { videos ->
                            allVideos.addAll(videos)
                            Log.d(TAG, "Found ${videos.size} videos from channel: $channelName")
                        }
                    } else {
                        Log.e(TAG, "API error for $channelName: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching from channel: $channelName", e)
                }
            }

            // Return random selection of videos
            return@withContext allVideos
                .distinctBy { it.id }
                .shuffled()
                .take(15)
                .also { results ->
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