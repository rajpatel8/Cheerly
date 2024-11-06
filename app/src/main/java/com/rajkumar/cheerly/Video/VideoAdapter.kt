package com.rajkumar.cheerly.Video

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
import com.rajkumar.cheerly.R

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
                    val videoId = videos[position].id
                    openVideo("https://www.youtube.com/watch?v=$videoId", itemView)
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

        // Set video title with ellipsis if too long
        holder.videoTitle.apply {
            text = video.title
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        // Set channel name
        holder.channelName.apply {
            text = video.channelName
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        // Load thumbnail using Coil
        holder.thumbnail.load(video.thumbnailUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.error_image)
        }

        // Show play icon
        holder.playIcon.visibility = View.VISIBLE

        // Add slight dark overlay to make play button more visible
        holder.thumbnail.colorFilter = android.graphics.ColorMatrixColorFilter(
            floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 0.8f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 0.7f, 0f // Increased darkness for better contrast
            )
        )

        // Add ripple effect to card
        holder.cardView.apply {
            isClickable = true
            isFocusable = true
            foreground = context.getDrawable(R.drawable.ripple_effect)
        }
    }

    override fun getItemCount() = videos.size

    private fun openVideo(videoUrl: String, view: View) {
        try {
            // First try to open in YouTube app
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.google.android.youtube")
                data = Uri.parse(videoUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            view.context.startActivity(intent)
        } catch (e: Exception) {
            // If YouTube app is not installed, open in browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            view.context.startActivity(webIntent)
        }
    }
}