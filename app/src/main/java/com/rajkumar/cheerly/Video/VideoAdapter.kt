package com.rajkumar.cheerly.Video

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.rajkumar.cheerly.R
class VideoAdapter(private val videos: List<Video>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
        val thumbnailImage: ImageView = view.findViewById(R.id.videoThumbnail)
        val playIcon: ImageView = view.findViewById(R.id.playIcon)
        val titleText: TextView = view.findViewById(R.id.videoTitle)
        val channelText: TextView = view.findViewById(R.id.channelName)

        init {
            cardView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openVideo(videos[position], it.context)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]

        // Set title
        holder.titleText.apply {
            text = video.title
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        // Set channel name
        holder.channelText.apply {
            text = video.channelName
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        // Load thumbnail
        holder.thumbnailImage.load(video.thumbnailUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.error_image)
        }

        // Add dark overlay for better play icon visibility
        holder.thumbnailImage.colorFilter = android.graphics.ColorMatrixColorFilter(
            floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 0.8f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 0.7f, 0f
            )
        )

        // Show play icon
        holder.playIcon.visibility = View.VISIBLE

        // Add ripple effect
        holder.cardView.apply {
            isClickable = true
            isFocusable = true
            foreground = ContextCompat.getDrawable(context, R.drawable.ripple_effect)
        }
    }

    override fun getItemCount() = videos.size

    private fun openVideo(video: Video, context: Context) {
        try {
            // First try to open in YouTube app
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl)).apply {
                setPackage("com.google.android.youtube")
            }
            if (appIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(appIntent)
            } else {
                // If YouTube app is not installed, open in browser
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            // If anything fails, try the browser as last resort
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open video", Toast.LENGTH_SHORT).show()
            }
        }
    }
}