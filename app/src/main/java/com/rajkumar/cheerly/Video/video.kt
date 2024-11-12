package com.rajkumar.cheerly.Video

data class Video(
    val id: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val videoUrl: String
)

// YouTube API Response Models
data class YouTubeSearchResponse(
    val kind: String,
    val etag: String,
    val nextPageToken: String?,
    val regionCode: String?,
    val pageInfo: PageInfo,
    val items: List<YouTubeVideo>
)

data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

data class YouTubeVideo(
    val kind: String,
    val etag: String,
    val id: VideoId,
    val snippet: VideoSnippet
)

data class VideoId(
    val kind: String,
    val videoId: String
)

data class VideoSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String,
    val liveBroadcastContent: String
)

data class Thumbnails(
    val default: ThumbnailDetails,
    val medium: ThumbnailDetails,
    val high: ThumbnailDetails
)

data class ThumbnailDetails(
    val url: String,
    val width: Int,
    val height: Int
)