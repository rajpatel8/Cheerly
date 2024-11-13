package com.rajkumar.cheerly.Podcast

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

            // Handle click with error handling
            holder.cardView.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(podcast.link))
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Log.d(TAG, "Error opening podcast link: ${e.message}")
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "Error binding podcast at position $position: ${e.message}")
        }
    }

    override fun getItemCount() = podcasts.size
}