package com.rajkumar.cheerly

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromptFragmentTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rajkumar.cheerly", appContext.packageName)
    }

    @Test
    fun testEditTextInputAndUIUpdates() {
        // Launch PromptFragment
        val scenario = launchFragmentInContainer<PromptFragment>(Bundle(), R.style.Theme_Cheerly)

        // Verify EditText input and UI updates
        val inputText = "Feeling great!"
        onView(withId(R.id.etMoodDescription)).perform(typeText(inputText))
        onView(withId(R.id.etMoodDescription)).check(matches(withText(inputText)))
    }
}
