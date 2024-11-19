package com.rajkumar.cheerly.TabLayout.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rajkumar.cheerly.R
import com.rajkumar.cheerly.Video.VideoAdapter
import com.rajkumar.cheerly.Video.VideoRepository
import kotlinx.coroutines.launch

class VideosFragment : Fragment() {
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var sectionTitle: TextView
    private var selectedMood: String = "Happy"
    private lateinit var videoRepository: VideoRepository

    companion object {
        private const val TAG = "VideosFragment"

        fun newInstance(): VideosFragment {
            return VideosFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        initializeRepository()
        loadInitialContent()
    }

    private fun initializeViews(view: View) {
        videoRecyclerView = view.findViewById(R.id.videoRecyclerView)
        progressBar = view.findViewById(R.id.videoProgressBar)
        sectionTitle = view.findViewById(R.id.videoSectionTitle)
    }

    private fun initializeRepository() {
        try {
            videoRepository = VideoRepository.getInstance()
            Log.d(TAG, "Successfully initialized VideoRepository")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing VideoRepository", e)
            showError("Error initializing video recommendations")
        }
    }

    private fun setupRecyclerView() {
        videoRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun loadInitialContent() {
        parentFragment?.arguments?.getString("selectedMood")?.let { mood ->
            selectedMood = mood
        }
        loadVideoRecommendations()
    }

    private fun loadVideoRecommendations() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                Log.d(TAG, "Starting video recommendations fetch for mood: $selectedMood")

                val videos = videoRepository.getVideoRecommendations(selectedMood)

                if (videos.isNotEmpty()) {
                    videoRecyclerView.adapter = VideoAdapter(videos)
                    sectionTitle.text = "Video Recommendations"
                    showContent()
                    Log.d(TAG, "Successfully loaded ${videos.size} videos")
                } else {
                    showError("No videos found for your mood")
                    Log.d(TAG, "No videos found for mood: $selectedMood")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading videos", e)
                showError("Error loading video recommendations")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            videoRecyclerView.visibility = View.GONE
            sectionTitle.visibility = View.GONE
        }
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        videoRecyclerView.visibility = View.VISIBLE
        sectionTitle.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        videoRecyclerView.visibility = View.GONE
        sectionTitle.visibility = View.GONE
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    fun refreshContent(mood: String) {
        selectedMood = mood
        loadVideoRecommendations()
    }
}