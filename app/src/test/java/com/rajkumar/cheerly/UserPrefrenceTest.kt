package com.rajkumar.cheerly

import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPrefrenceTest {

    @get:Rule
    val activityRule = ActivityTestRule(UserPrefrence::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rajkumar.cheerly", appContext.packageName)
    }

    @Test
    fun testButtonClickEventsAndUIUpdates() {
        // Launch UserPrefrence activity
        val scenario = ActivityScenario.launch(UserPrefrence::class.java)

        // Verify button click events and UI updates
        onView(withId(R.id.btn_rock)).perform(click())
        onView(withId(R.id.btn_rock)).check(matches(isSelected()))

        onView(withId(R.id.btn_jazz)).perform(click())
        onView(withId(R.id.btn_jazz)).check(matches(isSelected()))

        onView(withId(R.id.btn_classical)).perform(click())
        onView(withId(R.id.btn_classical)).check(matches(isSelected()))

        onView(withId(R.id.btn_hip_hop)).perform(click())
        onView(withId(R.id.btn_hip_hop)).check(matches(isSelected()))

        onView(withId(R.id.btn_country)).perform(click())
        onView(withId(R.id.btn_country)).check(matches(isSelected()))

        onView(withId(R.id.btn_action)).perform(click())
        onView(withId(R.id.btn_action)).check(matches(isSelected()))

        onView(withId(R.id.btn_comedy)).perform(click())
        onView(withId(R.id.btn_comedy)).check(matches(isSelected()))

        onView(withId(R.id.btn_sci_fi)).perform(click())
        onView(withId(R.id.btn_sci_fi)).check(matches(isSelected()))

        onView(withId(R.id.btn_thriller)).perform(click())
        onView(withId(R.id.btn_thriller)).check(matches(isSelected()))

        onView(withId(R.id.btn_horror)).perform(click())
        onView(withId(R.id.btn_horror)).check(matches(isSelected()))

        onView(withId(R.id.btn_technology)).perform(click())
        onView(withId(R.id.btn_technology)).check(matches(isSelected()))

        onView(withId(R.id.btn_health)).perform(click())
        onView(withId(R.id.btn_health)).check(matches(isSelected()))

        onView(withId(R.id.btn_sports)).perform(click())
        onView(withId(R.id.btn_sports)).check(matches(isSelected()))

        onView(withId(R.id.btn_business)).perform(click())
        onView(withId(R.id.btn_business)).check(matches(isSelected()))

        onView(withId(R.id.btn_education)).perform(click())
        onView(withId(R.id.btn_education)).check(matches(isSelected()))

        onView(withId(R.id.btn_gym)).perform(click())
        onView(withId(R.id.btn_gym)).check(matches(isSelected()))

        onView(withId(R.id.btn_park)).perform(click())
        onView(withId(R.id.btn_park)).check(matches(isSelected()))

        onView(withId(R.id.btn_cafe)).perform(click())
        onView(withId(R.id.btn_cafe)).check(matches(isSelected()))

        onView(withId(R.id.btn_library)).perform(click())
        onView(withId(R.id.btn_library)).check(matches(isSelected()))

        onView(withId(R.id.btn_mall)).perform(click())
        onView(withId(R.id.btn_mall)).check(matches(isSelected()))

        // Verify Next button click event
        onView(withId(R.id.btn_next)).perform(click())
        onView(withText("Please select an option from each group")).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }
}
