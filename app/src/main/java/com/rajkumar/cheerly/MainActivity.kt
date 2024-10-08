package com.rajkumar.cheerly

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Import LocalContext to show Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajkumar.cheerly.ui.theme.CheerlyTheme

// Main Activity Class
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheerlyTheme {
                // Scaffold provides the basic structure for the screen
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// MainScreen Composable Function
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the Logo
        Text(
            text = "Cheerly",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EA),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Visual Separator using Divider
        Divider(
            color = Color(0xFF6200EA),
            thickness = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Instruction Text
        Text(
            text = "Please select your favorite genres for Music, Videos, Podcasts, and Activities.",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Sections for Different Categories
        // Music Section
        SectionTitle(title = "Music")
        OptionRow(optionList = listOf("Rock", "Jazz", "Classical"), isRounded = true) // Row 1: 3 options
        OptionRow(optionList = listOf("Hip-Hop", "Country"), isRounded = true, centered = true) // Row 2: 2 centered options
        Spacer(modifier = Modifier.height(16.dp)) // Add spacing between sections

        // Videos Section
        SectionTitle(title = "Videos")
        OptionRow(optionList = listOf("Action", "Comedy", "Sci-Fi"), isRounded = true) // Row 1: 3 options
        OptionRow(optionList = listOf("Thriller", "Horror"), isRounded = true, centered = true) // Row 2: 2 centered options
        Spacer(modifier = Modifier.height(16.dp))

        // Podcast Section
        SectionTitle(title = "Podcasts")
        OptionRow(optionList = listOf("Technology", "Health", "Sports"), isRounded = true) // Row 1: 3 options
        OptionRow(optionList = listOf("Business", "Education"), isRounded = true, centered = true) // Row 2: 2 centered options
        Spacer(modifier = Modifier.height(16.dp))

        // Activity Section
        SectionTitle(title = "Activities")
        OptionRow(optionList = listOf("Gym", "Park", "Cafe"), isRounded = true) // Row 1: 3 options
        OptionRow(optionList = listOf("Library", "Mall"), isRounded = true, centered = true) // Row 2: 2 centered options
    }
}

// Composable Function to Display Section Titles
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = Color(0xFF6200EA)
    )
}

// Composable Function to Display Option Rows
@Composable
fun OptionRow(
    optionList: List<String>,
    isRounded: Boolean = false, // Controls whether the options have rounded corners
    centered: Boolean = false,  // Determines if the row should be centered
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Padding for space between rows
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        optionList.forEach { option ->
            SelectableOptionBox(optionText = option, isRounded = isRounded)
        }
    }
}

// Composable Function for Each Selectable Option Box
@Composable
fun SelectableOptionBox(optionText: String, isRounded: Boolean = false) {
    var isSelected by remember { mutableStateOf(false) } // Holds selection state
    val context = LocalContext.current // Context for displaying Toast messages

    Box(
        modifier = Modifier
            .size(100.dp, 50.dp) // Size of the option box
            .padding(horizontal = if (isRounded) 8.dp else 0.dp) // Space between boxes if rounded
            .background(
                color = if (isSelected) Color(0xFF6200EA) else Color(0xFFF1F1F1), // Background changes based on selection
                shape = RoundedCornerShape(
                    topEnd = 24.dp,
                    topStart = 24.dp,
                    bottomEnd = 24.dp,
                    bottomStart = 24.dp
                )
            )
            .clickable {
                isSelected = !isSelected
                if (isSelected) {
                    // Show a Toast message when the option is selected
                    Toast.makeText(context, "$optionText selected!", Toast.LENGTH_SHORT).show()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Display the option text inside the box
        Text(
            text = optionText,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF6200EA),
            fontSize = 14.sp
        )
    }
}

// Preview Function for the MainScreen Composable
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CheerlyTheme {
        MainScreen()
    }
}

