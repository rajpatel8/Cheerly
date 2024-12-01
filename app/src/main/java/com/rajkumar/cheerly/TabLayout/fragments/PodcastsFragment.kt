package com.rajkumar.cheerly.TabLayout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rajkumar.cheerly.MoodRecommendationActivity
import com.rajkumar.cheerly.Podcast.PodcastAdapter
import com.rajkumar.cheerly.Podcast.TeddyPodcastRepository
import com.rajkumar.cheerly.R
import kotlinx.coroutines.launch

class PodcastsFragment : Fragment() {
    private lateinit var podcastRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var selectedMood: String = "Happy"
    private lateinit var podcastRepository: TeddyPodcastRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_podcasts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        podcastRecyclerView = view.findViewById(R.id.podcastRecyclerView)
        progressBar = view.findViewById(R.id.podcastProgressBar)

        // Initialize repository
        podcastRepository = TeddyPodcastRepository.getInstance()

        // Setup RecyclerView
        podcastRecyclerView.layoutManager = LinearLayoutManager(context)

        // Get mood from activity
        selectedMood = (activity as? MoodRecommendationActivity)?.getSelectedMood() ?: "Happy"

        // Load podcast recommendations
        loadPodcastRecommendations()
    }

    private fun loadPodcastRecommendations() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE

                val podcasts = podcastRepository.getPodcastRecommendations(selectedMood)
                if (podcasts.isNotEmpty()) {
                    podcastRecyclerView.adapter = PodcastAdapter(podcasts)
                }

            } catch (e: Exception) {
                // Handle error
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    fun refreshContent(mood: String) {
        selectedMood = mood
        loadPodcastRecommendations()
    }
}