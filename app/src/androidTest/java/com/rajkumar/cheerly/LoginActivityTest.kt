package com.rajkumar.cheerly

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setup() {
        // Clear any previous states if needed
    }

    @Test
    fun testInitialUIState() {
        // Verify initial visibility and enabled states of buttons
        onView(withId(R.id.btnSpotifyLogin))
            .check(matches(isEnabled()))
            .check(matches(withText(R.string.connect_spotify)))

        onView(withId(R.id.btnYouTubeLogin))
            .check(matches(isEnabled()))
            .check(matches(withText(R.string.connect_youtube)))

        onView(withId(R.id.btnContinue))
            .check(matches(not(isEnabled())))

        // Check initial status texts
        onView(withId(R.id.tvSpotifyStatus))
            .check(matches(withText("× Spotify Not Connected")))

        onView(withId(R.id.tvYouTubeStatus))
            .check(matches(withText("× YouTube Not Connected")))

        // Check progress indicators are hidden
        onView(withId(R.id.progressSpotify))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        onView(withId(R.id.progressYouTube))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testSpotifyLoginButtonClick() {
        onView(withId(R.id.btnSpotifyLogin)).perform(click())

        // Verify button is disabled and progress is shown
        onView(withId(R.id.btnSpotifyLogin))
            .check(matches(not(isEnabled())))

        onView(withId(R.id.progressSpotify))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testYouTubeLoginButtonClick() {
        onView(withId(R.id.btnYouTubeLogin)).perform(click())

        // Verify button is disabled and progress is shown
        onView(withId(R.id.btnYouTubeLogin))
            .check(matches(not(isEnabled())))

        onView(withId(R.id.progressYouTube))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testContinueButtonNavigatesToMainActivity() {
        // Note: This test will need modification based on your actual navigation logic
        onView(withId(R.id.btnContinue)).perform(click())

        // Add assertions for navigation
        // You might need to use ActivityMonitor or similar to verify navigation
    }
}