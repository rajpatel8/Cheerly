package com.rajkumar.cheerly.Activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.button.MaterialButton
import com.rajkumar.cheerly.R
import com.rajkumar.cheerly.Activity.Models.NearbyActivity
import kotlin.math.roundToInt

class ActivityAdapter(private val activities: List<NearbyActivity>) :
    RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val imageContainer: FrameLayout = view.findViewById(R.id.imageContainer)
        val activityImage: ImageView = view.findViewById(R.id.activityImage)
        val activityTitle: TextView = view.findViewById(R.id.activityTitle)
        val activityCategory: TextView = view.findViewById(R.id.activityCategory)
        val activityDistance: TextView = view.findViewById(R.id.activityDistance)
        val activityAddress: TextView = view.findViewById(R.id.activityAddress)
        val weatherIcon: ImageView = view.findViewById(R.id.weatherIcon)
        val weatherText: TextView = view.findViewById(R.id.weatherText)
        val navigateButton: MaterialButton = view.findViewById(R.id.navigateButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]

        if (!activity.imageUrl.isNullOrEmpty()) {
            holder.imageContainer.visibility = View.VISIBLE
            holder.activityImage.load(activity.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.error_image)
            }
        } else {
            holder.imageContainer.visibility = View.GONE
        }

        // Set activity title
        holder.activityTitle.text = activity.name

        // Set category
        holder.activityCategory.text = activity.category
            .split("_")
            .joinToString(" ") { it.capitalize() }

        // Format and set distance
        val distanceText = when {
            activity.distance < 1.0 -> "${(activity.distance * 1000).roundToInt()} m"
            else -> String.format("%.1f km", activity.distance)
        }
        holder.activityDistance.text = "$distanceText away"

        // Set address
        holder.activityAddress.text = activity.address

        // Weather information
        activity.weather?.let { weather ->
            holder.weatherIcon.visibility = View.VISIBLE
            holder.weatherText.visibility = View.VISIBLE

            holder.weatherIcon.load("https://openweathermap.org/img/w/${weather.icon}.png") {
                crossfade(true)
                placeholder(R.drawable.ic_weather)
            }

            holder.weatherText.text = String.format("%.1f°C, %s\n%s",
                weather.temperature,
                weather.description.capitalize(),
                if (weather.isGoodForActivity) "Good for visiting" else "Check weather before going"
            )
        } ?: run {
            holder.weatherIcon.visibility = View.GONE
            holder.weatherText.visibility = View.GONE
        }

        // Click handlers
        holder.cardView.setOnClickListener {
            val context = holder.itemView.context
            if (context is FragmentActivity) {
                ActivityDetailDialog.newInstance(activity)
                    .show(context.supportFragmentManager, "activity_detail")
            }
        }

        // Navigate button click handler
        holder.navigateButton.setOnClickListener {
            val context = holder.itemView.context
            ActivityDetailDialog.openInMaps(context, activity.address)
        }
    }

    override fun getItemCount() = activities.size
}