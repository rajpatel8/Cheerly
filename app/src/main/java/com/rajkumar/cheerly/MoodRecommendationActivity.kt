package com.rajkumar.cheerly

import android.os.Bundle
import androidx.fragment.app.FragmentActivity  // Changed from ComponentActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rajkumar.cheerly.TabLayout.adapters.ViewPagerAdapter
import com.rajkumar.cheerly.TabLayout.interfaces.TabChangeListener

class MoodRecommendationActivity : FragmentActivity(), TabChangeListener {  // Changed parent class
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var pagerAdapter: ViewPagerAdapter
    private var selectedMood: String = "Happy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_recommendation)

        // Set status bar and navigation bar colors
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_dark)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.Cheerly_Pink)

        // Get selected mood from intent
        intent.getStringExtra("selectedMood")?.let {
            selectedMood = it
        }

        // Initialize views
        initializeViews()
        setupViewPager()
        setupTabLayout()
    }

    private fun initializeViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupViewPager() {
        // Initialize the adapter
        pagerAdapter = ViewPagerAdapter(this, selectedMood)
        viewPager.adapter = pagerAdapter

        // Configure ViewPager2
        viewPager.apply {
            adapter = pagerAdapter
            // Optional: Set the number of pages to keep loaded on each side
            offscreenPageLimit = 1

            // Register page change callback if needed
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onTabSelected(position)
                }
            })
        }
    }

    private fun setupTabLayout() {
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                ViewPagerAdapter.MUSIC_PAGE -> "Music"
                ViewPagerAdapter.VIDEOS_PAGE -> "Videos"
                ViewPagerAdapter.PODCASTS_PAGE -> "Podcasts"
                ViewPagerAdapter.ACTIVITIES_PAGE -> "Activities"
                else -> ""
            }
        }.attach()

        // Add tab selected listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { onTabSelected(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // TabChangeListener implementation
    override fun onTabSelected(position: Int) {
        // Handle tab selection if needed
        when (position) {
            ViewPagerAdapter.MUSIC_PAGE -> {
                // Handle music tab selected
            }
            ViewPagerAdapter.VIDEOS_PAGE -> {
                // Handle videos tab selected
            }
            ViewPagerAdapter.PODCASTS_PAGE -> {
                // Handle podcasts tab selected
            }
            ViewPagerAdapter.ACTIVITIES_PAGE -> {
                // Handle activities tab selected
            }
        }
    }

    override fun refreshContent(mood: String) {
        selectedMood = mood
        // Update title
        findViewById<android.widget.TextView>(R.id.titleText).apply {
            text = "Recommendations for ${selectedMood.lowercase()} mood"
        }
        // Refresh all fragments with new mood
        pagerAdapter.refreshAllFragments(selectedMood)
    }

    // Public method to get selected mood (used by fragments)
    fun getSelectedMood(): String = selectedMood

    override fun onBackPressed() {
        when (viewPager.currentItem) {
            0 -> {
                // If we're on the first page, handle back as normal
                super.onBackPressed()
            }
            else -> {
                // If we're not on the first page, go to the previous page
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }

    companion object {
        private const val TAG = "MoodRecommendation"
    }
}