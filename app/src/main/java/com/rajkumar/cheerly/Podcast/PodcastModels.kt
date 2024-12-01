package com.rajkumar.cheerly.Podcast

data class TeddySearchResponse(
    val data: Data
)

data class PodcastSeries(
    val uuid: String,
    val name: String,
    val itunesId: String?,
    val description: String?,
    val imageUrl: String?,
    val itunesInfo: ItunesInfo?,
    val episodes: List<TeddyEpisode>?
)
data class PodcastResponse(
    val count: Int,
    val results: List<PodcastEpisode>
)
data class ItunesInfo(
    val uuid: String,
    val publisherName: String?,
    val baseArtworkUrlOf: String?
)

data class TeddyEpisode(
    val uuid: String,
    val name: String,
    val description: String?,
    val audioUrl: String?
)

data class PodcastEpisode(
    val id: String,
    val title_original: String,
    val description_original: String?,
    val thumbnail: String?,
    val audio: String?,
    val audio_length_sec: Int = 0,
    val explicit_content: Boolean = false,
    val link: String = "",
    val podcast: PodcastInfo
)

data class PodcastInfo(
    val id: String,
    val title_original: String,
    val publisher_original: String?,
    val thumbnail: String?
)
