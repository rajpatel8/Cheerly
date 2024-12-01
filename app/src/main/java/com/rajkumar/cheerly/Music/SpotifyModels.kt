package com.rajkumar.cheerly.Music

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist> = emptyList(),
    val album: Album,
    val external_urls: ExternalUrls,
    val isHeader: Boolean = false,
    val popularity: Int = 0,
    val duration_ms: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Track
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

data class Artist(
    val id: String = "",
    val name: String = "",
    val external_urls: ExternalUrls = ExternalUrls(""),
    val genres: List<String> = emptyList(),
    val images: List<Image> = emptyList(),
    val popularity: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Artist
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

data class Album(
    val id: String = "",
    val name: String = "",
    val images: List<Image> = emptyList(),
    val external_urls: ExternalUrls = ExternalUrls(""),
    val release_date: String? = null,
    val album_type: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Album
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}


data class Image(
    val url: String = "",
    val height: Int = 0,
    val width: Int = 0
)

data class ExternalUrls(
    val spotify: String = ""
)

data class SpotifyError(
    val error: Error
)

data class Error(
    val status: Int,
    val message: String
)

data class CreatePlaylistRequest(
    val name: String,
    val description: String,
    val public: Boolean = false
)

data class PlaylistResponse(
    val id: String,
    val name: String,
    val external_urls: ExternalUrls
)

data class AddTracksRequest(
    val uris: List<String>
)

data class AddTracksResponse(
    val snapshot_id: String
)

data class UserProfile(
    val id: String,
    val display_name: String
)

data class PlaylistsResponse(
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val id: String,
    val name: String,
    val tracks: PlaylistTracks
)

data class PlaylistTracks(
    val total: Int,
    val href: String
)

data class PlaylistTracksResponse(
    val items: List<PlaylistTrackItem>
)

data class PlaylistTrackItem(
    val track: Track
)
