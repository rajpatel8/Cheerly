package com.rajkumar.cheerly.config

object ApiConfig {
    // YouTube Data API v3 configuration
    const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
    const val YOUTUBE_API_KEY = "AIzaSyC_xEkXM8LJz0OvMmQLXP5eF1OTIM1tj4w"  // Keep as fallback

    // API request parameters
    const val DEFAULT_REGION_CODE = "IN"  // India
    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_MAX_RESULTS = 10
    const val DEFAULT_ORDER = "relevance"

    // API endpoints
    const val YOUTUBE_SEARCH_ENDPOINT = "search"
    const val YOUTUBE_VIDEOS_ENDPOINT = "videos"

    // OAuth2 scopes
    const val YOUTUBE_READONLY_SCOPE = "https://www.googleapis.com/auth/youtube.readonly"
    const val YOUTUBE_FORCE_SSL_SCOPE = "https://www.googleapis.com/auth/youtube.force-ssl"
}