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
import com.google.android.material.card.MaterialCardView

class VideoAdapter(private val videos: List<Video>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val videoTitle: TextView = view.findViewById(R.id.videoTitle)
        val channelName: TextView = view.findViewById(R.id.channelName)
        val thumbnail: ImageView = view.findViewById(R.id.videoThumbnail)
        val playIcon: ImageView = view.findViewById(R.id.playIcon)

        init {
            cardView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openVideo(videos[position].videoUrl, itemView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videos[position]

        // Set video title
        holder.videoTitle.text = video.title

        // Set channel name
        holder.channelName.text = video.channelName

        // Load thumbnail using Coil
        holder.thumbnail.load(video.thumbnailUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.error_image)
        }

        // Add slight dark overlay to make play button more visible
        holder.thumbnail.colorFilter = android.graphics.ColorMatrixColorFilter(
            floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 0.8f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    override fun getItemCount() = videos.size

    private fun openVideo(videoUrl: String, view: View) {
        try {
            // First try to open in YouTube app
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.google.android.youtube")
                data = Uri.parse(videoUrl)
            }
            view.context.startActivity(intent)
        } catch (e: Exception) {
            // If YouTube app is not installed, open in browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            view.context.startActivity(webIntent)
        }
    }
}