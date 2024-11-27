package com.rajkumar.cheerly.TabLayout.fragments

import android.content.ContentValues.TAG
import android.content.Context
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
import com.rajkumar.cheerly.MoodRecommendationActivity
import com.rajkumar.cheerly.Music.Album
import com.rajkumar.cheerly.Music.ExternalUrls
import com.rajkumar.cheerly.Music.SongAdapter
import com.rajkumar.cheerly.Music.SpotifyRepository
import com.rajkumar.cheerly.Music.Track
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

                // Get user's music preferences
                val userPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    ?.getStringSet("selectedMusicOptions", emptySet()) ?: emptySet()

                val tracks = spotifyRepository.getRecommendations(selectedMood)

                if (tracks.isNotEmpty()) {
                    // Group tracks by genre preferences
                    val genreSortedTracks = mutableListOf<Track>()

                    // Add tracks for each user preferred genre
                    userPreferences.forEach { genre ->
                        val genreTracks = tracks.filter { track ->
                            track.name.contains(genre, ignoreCase = true) ||
                                    track.album.name.contains(genre, ignoreCase = true) ||
                                    track.artists.any { it.name.contains(genre, ignoreCase = true) }
                        }

                        if (genreTracks.isNotEmpty()) {
                            // Add genre header
                            genreSortedTracks.add(Track(
                                id = "header_$genre",
                                name = "✧ $genre Music",
                                artists = emptyList(),
                                album = Album(
                                    id = "",
                                    name = "",
                                    images = emptyList(),
                                    external_urls = ExternalUrls("")
                                ),
                                external_urls = ExternalUrls(""),
                                isHeader = true
                            ))
                            // Add top 3 tracks for this genre
                            genreSortedTracks.addAll(genreTracks.take(3))
                        }
                    }

                    // Add remaining mood-based recommendations if needed
                    val remainingTracks = tracks.filter { track ->
                        !genreSortedTracks.any { it.id == track.id }
                    }

                    if (remainingTracks.isNotEmpty()) {
                        genreSortedTracks.add(Track(
                            id = "header_mood",
                            name = "✧ Based on your $selectedMood mood",
                            artists = emptyList(),
                            album = Album(
                                id = "",
                                name = "",
                                images = emptyList(),
                                external_urls = ExternalUrls("")
                            ),
                            external_urls = ExternalUrls(""),
                            isHeader = true
                        ))
                        genreSortedTracks.addAll(remainingTracks.take(5))
                    }

                    musicRecyclerView.adapter = SongAdapter(genreSortedTracks)
                } else {
                    // Show empty state
                    showEmptyState("No recommendations found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading music recommendations", e)
                showError("Unable to load recommendations")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showEmptyState(message: String) {
        // Implement empty state view
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        // Show error message to user
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun refreshContent(mood: String) {
        selectedMood = mood
        loadMusicRecommendations()
    }
}