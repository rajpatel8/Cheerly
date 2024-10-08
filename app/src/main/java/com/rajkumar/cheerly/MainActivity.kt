package com.rajkumar.cheerly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rajkumar.cheerly.ui.theme.CheerlyTheme

/**
 * MainActivity is the main entry point of the application which sets up the UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheerlyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Cheerly",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * A composable function that displays a greeting message.
 * @param name The name to be included in the greeting message.
 * @param modifier The modifier to be applied to the Text composable.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/**
 * A preview function for the Greeting composable.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CheerlyTheme {
        Greeting("Android")
    }
}