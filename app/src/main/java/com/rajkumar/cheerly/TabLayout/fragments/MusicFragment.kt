package com.rajkumar.cheerly.TabLayout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rajkumar.cheerly.MoodRecommendationActivity
import com.rajkumar.cheerly.Music.SongAdapter
import com.rajkumar.cheerly.Music.SpotifyRepository
import com.rajkumar.cheerly.R
import kotlinx.coroutines.launch

class MusicFragment : Fragment() {
    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var selectedMood: String = "Happy"
    private lateinit var spotifyRepository: SpotifyRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        musicRecyclerView = view.findViewById(R.id.musicRecyclerView)
        progressBar = view.findViewById(R.id.musicProgressBar)

        // Initialize repository
        spotifyRepository = SpotifyRepository.getInstance(requireContext())

        // Setup RecyclerView
        musicRecyclerView.layoutManager = LinearLayoutManager(context)

        // Get mood from activity
        selectedMood = (activity as? MoodRecommendationActivity)?.getSelectedMood() ?: "Happy"

        // Load music recommendations
        loadMusicRecommendations()
    }

    private fun loadMusicRecommendations() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE

                val tracks = spotifyRepository.getRecommendations(selectedMood)
                if (tracks.isNotEmpty()) {
                    musicRecyclerView.adapter = SongAdapter(tracks)
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
        loadMusicRecommendations()
    }
}