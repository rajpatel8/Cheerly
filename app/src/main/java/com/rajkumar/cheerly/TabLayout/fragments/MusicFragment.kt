package com.rajkumar.cheerly.TabLayout.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var createPlaylistButton: MaterialButton
    private var selectedMood: String = "Happy"
    private lateinit var spotifyRepository: SpotifyRepository
    private var currentTracks: List<Track> = emptyList()

    companion object {
        private const val TAG = "MusicFragment"
    }

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
        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)

        // Initialize repository
        spotifyRepository = SpotifyRepository.getInstance(requireContext())

        // Setup RecyclerView
        musicRecyclerView.layoutManager = LinearLayoutManager(context)

        // Setup create playlist button
        createPlaylistButton.setOnClickListener {
            createSpotifyPlaylist()
        }

        // Get mood from activity
        selectedMood = (activity as? MoodRecommendationActivity)?.getSelectedMood() ?: "Happy"

        // Load music recommendations
        loadMusicRecommendations()
    }

    private fun createSpotifyPlaylist() {
        if (currentTracks.isEmpty()) {
            showMessage("No tracks available to create playlist")
            return
        }

        createPlaylistButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = spotifyRepository.createOrUpdatePlaylist(selectedMood, currentTracks)
                result.fold(
                    onSuccess = { status ->
                        when (status) {
                            "new" -> showMessage("Playlist 'Cheerly-$selectedMood' created successfully!")
                            "existing" -> showMessage("Added new songs to existing 'Cheerly-$selectedMood' playlist")
                            "no_new_tracks" -> showMessage("All recommended songs are already in the playlist")
                            else -> showMessage("Playlist updated successfully")
                        }
                    },
                    onFailure = { exception ->
                        showMessage("Failed to manage playlist: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                showMessage("Error managing playlist: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
                createPlaylistButton.isEnabled = true
            }
        }
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    fun loadMusicRecommendations() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                createPlaylistButton.isEnabled = false

                currentTracks = spotifyRepository.getRecommendations(selectedMood)
                    .filterNot { it.isHeader }

                if (currentTracks.isNotEmpty()) {
                    val displayTracks = createDisplayTracksWithHeaders(currentTracks)
                    musicRecyclerView.adapter = SongAdapter(displayTracks)
                    createPlaylistButton.isEnabled = true
                } else {
                    handleEmptyState()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading music recommendations", e)
                handleError(e)
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun createDisplayTracksWithHeaders(tracks: List<Track>): List<Track> {
        val displayTracks = mutableListOf<Track>()

        // Get user preferences
        val userPreferences = requireContext().getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
            .getStringSet("selectedMusicOptions", emptySet()) ?: emptySet()

        // Add tracks for each preference
        userPreferences.forEach { preference ->
            val preferredTracks = tracks.filter { track ->
                track.name.contains(preference, ignoreCase = true) ||
                        track.album.name.contains(preference, ignoreCase = true) ||
                        track.artists.any { it.name.contains(preference, ignoreCase = true) }
            }

            if (preferredTracks.isNotEmpty()) {
                // Add preference header
                displayTracks.add(createHeaderTrack("✧ $preference Music"))
                displayTracks.addAll(preferredTracks.take(3))
            }
        }

        // Add remaining mood-based tracks
        val remainingTracks = tracks.filter { track ->
            !displayTracks.contains(track)
        }

        if (remainingTracks.isNotEmpty()) {
            displayTracks.add(createHeaderTrack("✧ Based on your $selectedMood mood"))
            displayTracks.addAll(remainingTracks.take(7))
        }

        return displayTracks
    }

    private fun createHeaderTrack(headerText: String): Track {
        return Track(
            id = "header_${headerText.hashCode()}",
            name = headerText,
            artists = emptyList(),
            album = Album(
                id = "",
                name = "",
                images = emptyList(),
                external_urls = ExternalUrls("")
            ),
            external_urls = ExternalUrls(""),
            isHeader = true
        )
    }

    private fun handleEmptyState() {
        createPlaylistButton.isEnabled = false
        showMessage("No recommendations found")
    }

    private fun handleError(error: Exception) {
        createPlaylistButton.isEnabled = false
        showMessage("Unable to load recommendations: ${error.message}")
    }

    fun refreshContent(mood: String) {
        selectedMood = mood
        loadMusicRecommendations()
    }
}