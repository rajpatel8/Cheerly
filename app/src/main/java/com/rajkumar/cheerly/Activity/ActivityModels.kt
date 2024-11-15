package com.rajkumar.cheerly.Activity.Models

// Base Models
data class ActivityLocation(
    val latitude: Double,
    val longitude: Double
)

data class NearbyActivity(
    val id: String,
    val name: String,
    val type: String,
    val category: String,
    val distance: Double,
    val address: String,
    val rating: Float? = null,
    val imageUrl: String? = null,
    val openNow: Boolean = true,
    val weather: WeatherInfo? = null,
    val externalLink: String? = null,
    val placeId: String? = null,
    val contextualTips: List<String> = emptyList()
)

data class WeatherInfo(
    val temperature: Double,
    val description: String,
    val icon: String,
    val isGoodForActivity: Boolean
)

data class ActivityParameters(
    val categories: List<String>,
    val keywords: List<String>,
    val maxDistance: Double,
    val preferIndoor: Boolean,
    val preferPopular: Boolean
)
// Event Models
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
    val classifications: List<Classification>? = null,
    val priceRanges: List<PriceRange>? = null,
    val _embedded: EventVenueEmbedded? = null,
    val info: String? = null
)

data class Classification(
    val primary: Boolean,
    val segment: Segment,
    val genre: Genre? = null
)

data class Segment(
    val id: String,
    val name: String
)

data class Genre(
    val id: String,
    val name: String
)

data class EventImage(
    val url: String,
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

data class PriceRange(
    val type: String,
    val currency: String,
    val min: Double,
    val max: Double
)

data class EventVenueEmbedded(
    val venues: List<Venue>
)

data class Venue(
    val name: String,
    val address: Address,
    val city: City,
    val state: State,
    val location: VenueLocation?,
    val url: String?
)

data class Address(
    val line1: String
)

data class City(
    val name: String
)

data class State(
    val name: String,
    val stateCode: String
)

data class VenueLocation(
    val latitude: Double,
    val longitude: Double
)

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

// Weather Models
data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherDescription>,
    val wind: Wind,
    val clouds: Clouds,
    val visibility: Int
)

data class WeatherMain(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Clouds(
    val all: Int
)

// Enums
enum class PricePreference {
    LOW,
    MEDIUM,
    HIGH,
    ANY
}

enum class TimePreference {
    MORNING,
    AFTERNOON,
    EVENING,
    ANY
}