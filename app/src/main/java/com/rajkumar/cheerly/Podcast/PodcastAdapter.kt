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

        // Set podcast title
        holder.podcastTitle.text = podcast.title_original
        holder.podcastTitle.visibility = View.VISIBLE

        // Set publisher name
        holder.publisherName.text = podcast.podcast.publisher_original
        holder.publisherName.visibility = View.VISIBLE

        // Load thumbnail
        holder.podcastThumbnail.load(podcast.thumbnail) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.error_image)
        }

        holder.cardView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(podcast.link))
            holder.itemView.context.startActivity(intent)
        }

        // For debugging
        Log.d("PodcastAdapter", """
            Binding podcast at position $position:
            Title: ${podcast.title_original}
            Publisher: ${podcast.podcast.publisher_original}
        """.trimIndent())
    }

    override fun getItemCount() = podcasts.size
}