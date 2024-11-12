package com.rajkumar.cheerly.Video

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
                    openVideo(videos[position])
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

    private fun openVideo(video: Video) {
        val context = currentView?.context ?: return
        try {
            // Try to open in YouTube app
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.google.android.youtube")
                data = Uri.parse(video.videoUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
            context.startActivity(webIntent)
        }
    }

    private val currentView: View?
        get() = if (itemCount > 0) {
            (0 until itemCount)
                .firstOrNull { position ->
                    getViewHolderForPosition(position)?.itemView != null
                }
                ?.let { getViewHolderForPosition(it)?.itemView }
        } else null

    private fun getViewHolderForPosition(position: Int): VideoViewHolder? {
        return itemCount.takeIf { it > position }?.let {
            getViewHolderForAdapterPosition(position)
        }
    }

    private fun getViewHolderForAdapterPosition(position: Int): VideoViewHolder? {
        return try {
            val recyclerView = currentView?.parent as? RecyclerView
            recyclerView?.findViewHolderForAdapterPosition(position) as? VideoViewHolder
        } catch (e: Exception) {
            null
        }
    }
}