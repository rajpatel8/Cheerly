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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]

        // Set activity title
        holder.activityTitle.text = activity.name

        // Set category
        holder.activityCategory.text = activity.category.replace("_", " ").capitalize()

        // Set distance
        holder.activityDistance.text = String.format("%.1f km away", activity.distance)

        // Set address
        holder.activityAddress.text = activity.address

        // Load activity image if available
        activity.imageUrl?.let { url ->
            holder.activityImage.load(url) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.error_image)
            }
        } ?: run {
            // Load a default image based on activity type
            val defaultImageRes = when {
                activity.category.contains("park") -> R.drawable.ic_park
                activity.category.contains("restaurant") -> R.drawable.ic_restaurant
                activity.category.contains("event") -> R.drawable.ic_event
                else -> R.drawable.ic_activity
            }
            holder.activityImage.setImageResource(defaultImageRes)
        }

        // Show weather information if available
        activity.weather?.let { weather ->
            holder.weatherIcon.visibility = View.VISIBLE
            holder.weatherText.visibility = View.VISIBLE

            // Load weather icon
            holder.weatherIcon.load("https://openweathermap.org/img/w/${weather.icon}.png") {
                crossfade(true)
                placeholder(R.drawable.ic_weather)
            }

            holder.weatherText.text = String.format("%.1f°C, %s",
                weather.temperature,
                weather.description.capitalize()
            )
        } ?: run {
            holder.weatherIcon.visibility = View.GONE
            holder.weatherText.visibility = View.GONE
        }

        // Handle click events
        holder.cardView.setOnClickListener {
            activity.externalLink?.let { link ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = activities.size
}