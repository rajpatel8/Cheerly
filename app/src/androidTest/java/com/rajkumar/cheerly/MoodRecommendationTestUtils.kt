package com.rajkumar.cheerly.test

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Custom matchers and utilities for MoodRecommendationActivity testing
 */
object MoodRecommendationTestUtils {

    /**
     * Custom matcher to check if a RecyclerView has items
     */
    fun recyclerViewHasItems(): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("RecyclerView with items")
            }

            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                val adapter = recyclerView.adapter
                return adapter != null && adapter.itemCount > 0
            }
        }
    }

    /**
     * Custom matcher to check text in list items
     */
    fun withItemText(text: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("with item text: $text")
            }

            override fun matchesSafely(view: View): Boolean {
                return view is TextView && view.text.toString() == text
            }
        }
    }

    /**
     * Wait for a specific time
     */
    fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = ViewMatchers.isDisplayed()

            override fun getDescription(): String = "wait for $millis milliseconds"

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    /**
     * Check if a section is visible and has content
     */
    fun isSectionLoadedAndVisible(sectionTitleId: Int, recyclerViewId: Int): Boolean {
        try {
            Espresso.onView(ViewMatchers.withId(sectionTitleId))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            Espresso.onView(ViewMatchers.withId(recyclerViewId))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(recyclerViewHasItems()))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Extension function to perform multiple checks
     */
    fun ViewInteraction.checkAll(vararg assertions: ViewAssertion): ViewInteraction {
        assertions.forEach { check(it) }
        return this
    }

    /**
     * Custom IdlingResource for location updates
     */
    class LocationIdlingResource : IdlingResource {
        private var resourceCallback: IdlingResource.ResourceCallback? = null
        @Volatile private var isIdle = false

        override fun getName(): String = "Location update idling resource"

        override fun isIdleNow(): Boolean = isIdle

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            resourceCallback = callback
        }

        fun setIdleState(isIdleNow: Boolean) {
            isIdle = isIdleNow
            if (isIdleNow) {
                resourceCallback?.onTransitionToIdle()
            }
        }
    }
}