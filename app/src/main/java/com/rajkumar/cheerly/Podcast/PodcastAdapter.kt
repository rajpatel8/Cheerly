package com.rajkumar.cheerly.Podcast

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.rajkumar.cheerly.R

class PodcastAdapter(private val podcasts: List<PodcastEpisode>) :
    RecyclerView.Adapter<PodcastAdapter.ViewHolder>() {

    private val TAG = "PodcastAdapter"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val podcastTitle: TextView = view.findViewById(R.id.podcastTitle)
        val publisherName: TextView = view.findViewById(R.id.publisherName)
        val podcastThumbnail: ImageView = view.findViewById(R.id.podcastThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_podcast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val podcast = podcasts[position]

        try {
            // Set podcast title with ellipsis if too long
            holder.podcastTitle.apply {
                text = podcast.title_original
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Set publisher name with ellipsis if too long
            holder.publisherName.apply {
                text = podcast.podcast.publisher_original
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Load thumbnail with error handling
            holder.podcastThumbnail.load(podcast.thumbnail) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.error_image)
            }

            // Handle click with audio URL
            holder.cardView.setOnClickListener {
                try {
                    podcast.audio?.let { audioUrl ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(audioUrl))
                        // Try to open in browser if no dedicated app is found
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        holder.itemView.context.startActivity(intent)
                    } ?: run {
                        // If audio URL is null, show a message
                        Toast.makeText(
                            holder.itemView.context,
                            "Audio URL not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening podcast: ${e.message}")
                    Toast.makeText(
                        holder.itemView.context,
                        "Unable to open podcast",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding podcast at position $position: ${e.message}")
        }
    }

    override fun getItemCount() = podcasts.size
}