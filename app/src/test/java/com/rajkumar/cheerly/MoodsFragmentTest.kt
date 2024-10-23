package com.rajkumar.cheerly

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoodsFragmentTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rajkumar.cheerly", appContext.packageName)
    }

    @Test
    fun testButtonClickEventsAndUIUpdates() {
        // Launch MoodsFragment
        val scenario = launchFragmentInContainer<MoodsFragment>(Bundle(), R.style.Theme_Cheerly)

        // Verify button click events and UI updates
        onView(withId(R.id.btnHappy)).perform(click())
        onView(withId(R.id.btnHappy)).check(matches(isSelected()))

        onView(withId(R.id.btnSad)).perform(click())
        onView(withId(R.id.btnSad)).check(matches(isSelected()))

        onView(withId(R.id.btnExcited)).perform(click())
        onView(withId(R.id.btnExcited)).check(matches(isSelected()))

        onView(withId(R.id.btnRelaxed)).perform(click())
        onView(withId(R.id.btnRelaxed)).check(matches(isSelected()))
    }
}
