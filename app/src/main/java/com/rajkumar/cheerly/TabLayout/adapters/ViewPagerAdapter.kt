package com.rajkumar.cheerly.TabLayout.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rajkumar.cheerly.TabLayout.fragments.ActivitiesFragment
import com.rajkumar.cheerly.TabLayout.fragments.MusicFragment
import com.rajkumar.cheerly.TabLayout.fragments.PodcastsFragment
import com.rajkumar.cheerly.TabLayout.fragments.VideosFragment
import com.rajkumar.cheerly.MoodRecommendationActivity

class ViewPagerAdapter(
    private val activity: MoodRecommendationActivity,
    private val mood: String
) : FragmentStateAdapter(activity) {

    // Keep track of fragments for accessing them later
    private val fragments = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> MusicFragment()
            1 -> VideosFragment()
            2 -> PodcastsFragment()
            3 -> ActivitiesFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }

        // Store the fragment reference
        fragments[position] = fragment
        return fragment
    }

    // Get fragment at position
    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }

    // Refresh all fragments with new mood
    fun refreshAllFragments(newMood: String) {
        fragments.values.forEach { fragment ->
            when (fragment) {
                is MusicFragment -> fragment.refreshContent(newMood)
                is VideosFragment -> fragment.refreshContent(newMood)
                is PodcastsFragment -> fragment.refreshContent(newMood)
                is ActivitiesFragment -> fragment.refreshContent(newMood)
            }
        }
    }

    companion object {
        const val MUSIC_PAGE = 0
        const val VIDEOS_PAGE = 1
        const val PODCASTS_PAGE = 2
        const val ACTIVITIES_PAGE = 3
    }
}