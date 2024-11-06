package com.rajkumar.cheerly.Podcast

data class PodcastResponse(
    val count: Int,
    val results: List<PodcastEpisode>
)

data class PodcastEpisode(
    val id: String,
    val title_original: String,  // Changed from title to title_original
    val description_original: String, // Changed from description
    val thumbnail: String,
    val audio: String,
    val audio_length_sec: Int,
    val explicit_content: Boolean,
    val link: String,
    val podcast: PodcastInfo
)

data class PodcastInfo(
    val id: String,
    val title_original: String,  // Changed from title
    val publisher_original: String,  // Changed from publisher
    val thumbnail: String
)