package com.rajkumar.cheerly.Music

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val album: Album,
    val external_urls: ExternalUrls
)

data class Artist(
    val id: String,
    val name: String,
    val external_urls: ExternalUrls,
    val genres: List<String> = emptyList()
)

data class Album(
    val id: String,
    val name: String,
    val images: List<Image>,
    val external_urls: ExternalUrls
)

data class Image(
    val url: String,
    val height: Int,
    val width: Int
)

data class ExternalUrls(
    val spotify: String
)