package com.rajkumar.cheerly.Activity

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.rajkumar.cheerly.R
import com.rajkumar.cheerly.Activity.Models.NearbyActivity
import kotlin.math.roundToInt

class ActivityAdapter(private val activities: List<NearbyActivity>) :
    RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val activityTitle: TextView = view.findViewById(R.id.activityTitle)
        val activityCategory: TextView = view.findViewById(R.id.activityCategory)
        val activityDistance: TextView = view.findViewById(R.id.activityDistance)
        val activityAddress: TextView = view.findViewById(R.id.activityAddress)
        val activityImage: ImageView = view.findViewById(R.id.activityImage)
        val weatherIcon: ImageView = view.findViewById(R.id.weatherIcon)
        val weatherText: TextView = view.findViewById(R.id.weatherText)
        val mapsIcon: ImageView = view.findViewById(R.id.mapsIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]

        holder.activityTitle.text = activity.name

        holder.activityCategory.text = activity.category
            .split("_")
            .joinToString(" ") { it.capitalize() }

        // Format distance
        val distanceText = when {
            activity.distance < 1.0 -> "${(activity.distance * 1000).roundToInt()} m"
            else -> String.format("%.1f km", activity.distance)
        }
        holder.activityDistance.text = "$distanceText away"

        holder.activityAddress.text = activity.address

        // Load activity image
        activity.imageUrl?.let { url ->
            holder.activityImage.load(url) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.error_image)
            }
        } ?: run {
            // Default image based on category
            val defaultImageRes = when {
                activity.category.contains("park", ignoreCase = true) -> R.drawable.ic_park
                activity.category.contains("food", ignoreCase = true) -> R.drawable.ic_restaurant
                activity.category.contains("event", ignoreCase = true) -> R.drawable.ic_event
                else -> R.drawable.ic_activity
            }
            holder.activityImage.setImageResource(defaultImageRes)
        }

        // Weather information
        activity.weather?.let { weather ->
            holder.weatherIcon.visibility = View.VISIBLE
            holder.weatherText.visibility = View.VISIBLE

            holder.weatherIcon.load("https://openweathermap.org/img/w/${weather.icon}.png") {
                crossfade(true)
                placeholder(R.drawable.ic_weather)
            }

            val weatherStatus = if (weather.isGoodForActivity) "Good for visiting" else "Check before going"
            holder.weatherText.text = String.format("%.1f°C, %s\n%s",
                weather.temperature,
                weather.description.capitalize(),
                weatherStatus
            )
        } ?: run {
            holder.weatherIcon.visibility = View.GONE
            holder.weatherText.visibility = View.GONE
        }

        // Click handler for Google Maps navigation
        holder.cardView.setOnClickListener {
            val context = holder.itemView.context
            try {
                // Try to open in Google Maps app
                val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(activity.address))
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    // Fallback to browser using Google Maps URL
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(activity.address)}"))
                    context.startActivity(browserIntent)
                }
            } catch (e: Exception) {
                // Final fallback - open address in browser
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(activity.address)}"))
                context.startActivity(browserIntent)
            }
        }
    }

    override fun getItemCount() = activities.size
}