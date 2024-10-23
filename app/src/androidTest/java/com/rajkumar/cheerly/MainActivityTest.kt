package com.rajkumar.cheerly

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rajkumar.cheerly", appContext.packageName)
    }

    @Test
    fun testMainActivityLaunchesUserPrefrence() {
        // Launch MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Verify that UserPrefrence activity is launched
        scenario.onActivity { activity ->
            val expectedIntent = Intent(activity, UserPrefrence::class.java)
            val actualIntent = activity.intent
            assertEquals(expectedIntent.component, actualIntent.component)
        }
    }
}
