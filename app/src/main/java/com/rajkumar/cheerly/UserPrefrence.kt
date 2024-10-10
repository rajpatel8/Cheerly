package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class UserPrefrence : ComponentActivity() {

    private val selectedOptions = mutableSetOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.user_prefrence)

        setupButtons()

        // Find the button by its ID
        val btnNext = findViewById<Button>(R.id.btn_next)

        // Set up a click listener to navigate to PromptActivity
        btnNext.setOnClickListener {
            // Navigate to PromptActivity using an Intent
            val intent = Intent(this, PromptActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupButtons() {
        // Music buttons
        setupButtonGroup(listOf("Rock", "Jazz", "Classical", "Hip-Hop", "Country"))

        // Videos buttons
        setupButtonGroup(listOf("Action", "Comedy", "Sci-Fi", "Thriller", "Horror"))

        // Podcasts buttons
        setupButtonGroup(listOf("Technology", "Health", "Sports", "Business", "Education"))

        // Activities buttons
        setupButtonGroup(listOf("Gym", "Park", "Cafe", "Library", "Mall"))
    }

    private fun setupButtonGroup(options: List<String>) {
        options.forEach { option ->
            val buttonId = resources.getIdentifier("btn_${option.toLowerCase().replace("-", "_")}", "id", packageName)
            findViewById<Button>(buttonId)?.setOnClickListener { button ->
                toggleSelection(button as Button)
            }
        }
    }

    private fun toggleSelection(button: Button) {
        val option = button.text.toString()
        if (selectedOptions.contains(option)) {
            selectedOptions.remove(option)
            button.setBackgroundResource(R.drawable.rounded_button)
        } else {
            selectedOptions.add(option)
            button.setBackgroundResource(R.drawable.button_selected)
        }

        // Show a toast message when an option is selected or deselected
        val action = if (selectedOptions.contains(option)) "selected" else "deselected"
        Toast.makeText(this, "$option $action!", Toast.LENGTH_SHORT).show()
    }
}



