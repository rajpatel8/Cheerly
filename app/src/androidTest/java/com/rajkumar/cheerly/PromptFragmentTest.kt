package com.rajkumar.cheerly

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromptFragmentTest {

    @get:Rule
    val activityRule = ActivityTestRule(PromptActivity::class.java)

    @Test
    fun testEditTextInputAndUIUpdates() {
        // Type text into EditText and verify the input
        val inputText = "Feeling happy today!"
        onView(withId(R.id.etMoodDescription)).perform(typeText(inputText))
        onView(withId(R.id.etMoodDescription)).check(matches(withText(inputText)))
    }
}
