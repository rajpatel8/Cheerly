package com.rajkumar.cheerly

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class SongAdapter(private val tracks: List<Track>) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val artistName: TextView = view.findViewById(R.id.artistName)
        val albumArt: ImageView = view.findViewById(R.id.albumArt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]

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

    override fun getItemCount() = tracks.size
}