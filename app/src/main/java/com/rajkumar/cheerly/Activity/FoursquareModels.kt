package com.rajkumar.cheerly.Activity.Models

// Main response wrapper
data class FoursquareSearchResponse(
    val results: List<FoursquareVenue>
)

// Venue details
data class FoursquareVenue(
    val fsq_id: String,
    val name: String,
    val categories: List<FoursquareCategory>,
    val location: FoursquareLocation,
    val distance: Int,
    val rating: Double? = null,
    val photos: List<FoursquarePhoto>? = null
)

// Category information
data class FoursquareCategory(
    val id: Int,
    val name: String,
    val icon: FoursquareIcon? = null
)

// Location details
data class FoursquareLocation(
    val address: String? = null,
    val formatted_address: String,
    val country: String? = null,
    val locality: String? = null,
    val postcode: String? = null,
    val region: String? = null,
    val lat: Double,
    val lng: Double
)

// Photo information
data class FoursquarePhoto(
    val id: String,
    val created_at: String? = null,
    val prefix: String,
    val suffix: String,
    val width: Int? = null,
    val height: Int? = null
)

// Icon information
data class FoursquareIcon(
    val prefix: String,
    val suffix: String
)