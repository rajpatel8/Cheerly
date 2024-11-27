package com.rajkumar.cheerly.Music

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.rajkumar.cheerly.R

class SongAdapter(private val tracks: List<Track>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRACK = 1
    }

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val artistName: TextView = view.findViewById(R.id.artistName)
        val albumArt: ImageView = view.findViewById(R.id.albumArt)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val genreTitle: TextView = view.findViewById(R.id.genreTitle)
    }

    override fun getItemViewType(position: Int): Int {
        return if (tracks[position].isHeader) VIEW_TYPE_HEADER else VIEW_TYPE_TRACK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_song_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_song, parent, false)
                TrackViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val track = tracks[position]

        when (holder) {
            is HeaderViewHolder -> {
                holder.genreTitle.text = track.name
            }
            is TrackViewHolder -> {
                // Set song title
                holder.songTitle.text = track.name

                // Set artist names
                holder.artistName.text = track.artists.joinToString(", ") { it.name }

                // Load album art using Coil
                track.album.images.firstOrNull()?.let { image ->
                    holder.albumArt.load(image.url) {
                        crossfade(true)
                        placeholder(R.drawable.placeholder_image)
                        error(R.drawable.error_image)
                    }
                }

                // Open Spotify when clicked
                holder.itemView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(track.external_urls.spotify))
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount() = tracks.size
}