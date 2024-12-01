package com.rajkumar.cheerly

import android.Manifest
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoodRecommendationActivityTest {

    private val TEST_MOOD = "Happy"

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val activityRule = ActivityScenarioRule<MoodRecommendationActivity>(
        Intent(ApplicationProvider.getApplicationContext(), MoodRecommendationActivity::class.java)
            .putExtra("selectedMood", TEST_MOOD)
    )

    @Test
    fun testInitialUIState() {
        // Check title text
        onView(withId(R.id.titleText))
            .check(matches(isDisplayed()))
            .check(matches(withText("Recommendations for ${TEST_MOOD.lowercase()} mood")))

        // Check section titles are initially hidden
        onView(withId(R.id.musicSectionTitle))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.videoSectionTitle))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.podcastSectionTitle))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.activitySectionTitle))
            .check(matches(not(isDisplayed())))

        // Check RecyclerViews are initially hidden
        onView(withId(R.id.musicRecyclerView))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.videoRecyclerView))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.podcastRecyclerView))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.activityRecyclerView))
            .check(matches(not(isDisplayed())))

        // Check progress bars
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))
        onView(withId(R.id.locationProgressBar))
            .check(matches(isDisplayed()))

        // Check location status
        onView(withId(R.id.locationStatusText))
            .check(matches(isDisplayed()))
            .check(matches(withText("Finding nearby activities...")))
    }

    @Test
    fun testLoadingStateUI() {
        // Initial loading state
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))

        // Verify location progress is shown
        onView(withId(R.id.locationProgressBar))
            .check(matches(isDisplayed()))

        // Verify recycler views are not visible during loading
        onView(withId(R.id.musicRecyclerView))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.videoRecyclerView))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.podcastRecyclerView))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.activityRecyclerView))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun testLocationPermissionDeniedState() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MoodRecommendationActivity::class.java)
            .putExtra("selectedMood", TEST_MOOD)

        ActivityScenario.launch<MoodRecommendationActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                // Simulate permission denied callback
                activity.onRequestPermissionsResult(
                    MoodRecommendationActivity.LOCATION_PERMISSION_REQUEST_CODE,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    intArrayOf(android.content.pm.PackageManager.PERMISSION_DENIED)
                )
            }

            onView(withId(R.id.locationStatusText))
                .check(matches(withText("Location permission denied")))

            onView(withId(R.id.locationProgressBar))
                .check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testRecyclerViewsSetup() {
        activityRule.scenario.onActivity { activity ->
            // Verify RecyclerViews have LayoutManagers
            val musicRecyclerView = activity.findViewById<RecyclerView>(R.id.musicRecyclerView)
            val videoRecyclerView = activity.findViewById<RecyclerView>(R.id.videoRecyclerView)
            val podcastRecyclerView = activity.findViewById<RecyclerView>(R.id.podcastRecyclerView)
            val activityRecyclerView = activity.findViewById<RecyclerView>(R.id.activityRecyclerView)

            assert(musicRecyclerView.layoutManager != null)
            assert(videoRecyclerView.layoutManager != null)
            assert(podcastRecyclerView.layoutManager != null)
            assert(activityRecyclerView.layoutManager != null)
        }
    }

    @Test
    fun testDifferentMoodTitle() {
        val moods = listOf("Sad", "Excited", "Relaxed", "Bored", "Anxious", "Focused")

        moods.forEach { mood ->
            val intent = Intent(ApplicationProvider.getApplicationContext(), MoodRecommendationActivity::class.java)
                .putExtra("selectedMood", mood)

            ActivityScenario.launch<MoodRecommendationActivity>(intent).use {
                onView(withId(R.id.titleText))
                    .check(matches(withText("Recommendations for ${mood.lowercase()} mood")))
            }
        }
    }

    @Test
    fun testLocationStatusUpdates() {
        val statusMessages = listOf(
            "Finding nearby activities...",
            "Improving location accuracy...",
            "Using approximate location",
            "Found nearby activities"
        )

        statusMessages.forEach { message ->
            activityRule.scenario.onActivity { activity ->
                activity.findViewById<TextView>(R.id.locationStatusText).text = message
            }

            onView(withId(R.id.locationStatusText))
                .check(matches(withText(message)))
        }
    }

    private fun hasItems(): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("has items")
            }

            override fun matchesSafely(view: View): Boolean {
                return if (view is RecyclerView) {
                    view.adapter != null && view.adapter!!.itemCount > 0
                } else false
            }
        }
    }

    private fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "wait for $millis milliseconds"
            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }
}