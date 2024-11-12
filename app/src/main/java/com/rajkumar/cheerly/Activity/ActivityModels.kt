package com.rajkumar.cheerly.Activity

data class NearbyActivity(
    val id: String,
    val name: String,
    val type: String, // e.g., "event", "place", "venue"
    val category: String,
    val distance: Double,
    val address: String,
    val rating: Float? = null,
    val imageUrl: String? = null,
    val openNow: Boolean? = null,
    val weather: WeatherInfo? = null,
    val externalLink: String? = null
)

data class WeatherInfo(
    val temperature: Double,
    val description: String,
    val icon: String,
    val isGoodForActivity: Boolean
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

// OpenStreetMap response models
data class PlaceResponse(
    val elements: List<PlaceElement>
)

data class PlaceElement(
    val id: Long,
    val type: String,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>
)

// Ticketmaster response models
data class EventResponse(
    val _embedded: EventEmbedded
)

data class EventEmbedded(
    val events: List<Event>
)

data class Event(
    val id: String,
    val name: String,
    val url: String,
    val images: List<EventImage>,
    val dates: EventDates,
    val _embedded: EventVenueEmbedded? = null
)

data class EventImage(
    val url: String,
    val ratio: String,
    val width: Int,
    val height: Int
)

data class EventDates(
    val start: EventStart
)

data class EventStart(
    val localDate: String,
    val localTime: String
)

data class EventVenueEmbedded(
    val venues: List<Venue>
)

data class Venue(
    val name: String,
    val address: Address,
    val location: VenueLocation? = null
)

data class Address(
    val line1: String,
    val city: String,
    val state: String,
    val postalCode: String
)

data class VenueLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

// OpenWeatherMap response models
data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherDescription>
)

data class WeatherMain(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class WeatherDescription(
    val description: String,
    val icon: String
)
