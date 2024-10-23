package com.rajkumar.cheerly

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromptActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(PromptActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rajkumar.cheerly", appContext.packageName)
    }

    @Test
    fun testFragmentTransactionsAndUIUpdates() {
        // Launch PromptActivity
        val scenario = ActivityScenario.launch(PromptActivity::class.java)

        // Verify that MoodsFragment is loaded by default
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.btnMoods)).check(matches(isSelected()))
        onView(withId(R.id.btnPrompt)).check(matches(isNotSelected()))

        // Click on Prompt button and verify that PromptFragment is loaded
        onView(withId(R.id.btnPrompt)).perform(click())
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.btnPrompt)).check(matches(isSelected()))
        onView(withId(R.id.btnMoods)).check(matches(isNotSelected()))

        // Click on Moods button and verify that MoodsFragment is loaded again
        onView(withId(R.id.btnMoods)).perform(click())
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.btnMoods)).check(matches(isSelected()))
        onView(withId(R.id.btnPrompt)).check(matches(isNotSelected()))
    }
}
