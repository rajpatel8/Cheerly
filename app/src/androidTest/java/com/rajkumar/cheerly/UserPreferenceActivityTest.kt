package com.rajkumar.cheerly

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Matcher

@RunWith(AndroidJUnit4::class)
class UserPreferenceActivityTest {

    private lateinit var scenario: ActivityScenario<UserPrefrence>
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        scenario = ActivityScenario.launch(UserPrefrence::class.java)
    }

    // Custom matcher for background resource
    private fun withBackgroundResource(resourceId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("with background resource id: $resourceId")
            }

            override fun matchesSafely(view: View): Boolean {
                return try {
                    val background = view.background
                    background != null && view.context.resources.getResourceEntryName(resourceId) ==
                            view.context.resources.getResourceEntryName(background.constantState!!.changingConfigurations)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun testInitialButtonStates() {
        // Verify initial state of category buttons
        val categories = listOf("rock", "jazz", "classical", "hip_hop", "country")
        categories.forEach { category ->
            onView(withId(getResourceId("btn_$category")))
                .check(matches(isDisplayed()))
        }

        // Verify next button is enabled but grey
        onView(withId(R.id.btn_next))
            .check(matches(isEnabled()))
    }

    @Test
    fun testButtonSelection() {
        // Test selecting buttons from each category
        onView(withId(getResourceId("btn_rock"))).perform(click())
        onView(withId(getResourceId("btn_action"))).perform(click())
        onView(withId(getResourceId("btn_technology"))).perform(click())
        onView(withId(getResourceId("btn_gym"))).perform(click())

        // Verify all buttons are selected (you can verify this through the SharedPreferences)
        scenario.onActivity { activity ->
            val prefs = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            assert(prefs.getStringSet("selectedMusicOptions", emptySet())?.isNotEmpty() == true)
            assert(prefs.getStringSet("selectedVideoOptions", emptySet())?.isNotEmpty() == true)
            assert(prefs.getStringSet("selectedPodcastOptions", emptySet())?.isNotEmpty() == true)
            assert(prefs.getStringSet("selectedActivityOptions", emptySet())?.isNotEmpty() == true)
        }
    }

    @Test
    fun testNavigationOnCompleteSelection() {
        // Select one option from each category
        onView(withId(getResourceId("btn_rock"))).perform(click())
        onView(withId(getResourceId("btn_action"))).perform(click())
        onView(withId(getResourceId("btn_technology"))).perform(click())
        onView(withId(getResourceId("btn_gym"))).perform(click())

        // Click next button
        onView(withId(R.id.btn_next)).perform(click())

        // Verify shared preferences are set
        scenario.onActivity { activity ->
            val prefs = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            assert(prefs.getBoolean("isUserPreferenceSet", false))
        }
    }

    @Test
    fun testIncompleteSelectionState() {
        // Select only two categories
        onView(withId(getResourceId("btn_rock"))).perform(click())
        onView(withId(getResourceId("btn_action"))).perform(click())

        // Click next button - should show toast
        onView(withId(R.id.btn_next)).perform(click())

        // Verify that the preferences haven't been set
        scenario.onActivity { activity ->
            val prefs = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            assert(!prefs.getBoolean("isUserPreferenceSet", false))
        }
    }

    private fun getResourceId(id: String): Int {
        return context.resources.getIdentifier(id, "id", context.packageName)
    }
}