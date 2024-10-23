package com.rajkumar.cheerly

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoodsFragmentTest {

    @Test
    fun testButtonClickEventsAndUIUpdates() {
        // Launch MoodsFragment
        val scenario = launchFragmentInContainer<MoodsFragment>()

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
