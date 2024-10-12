package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class UserPrefrence : ComponentActivity() {

    private var visiblity = false

    // Track selected options for each group
    private val selectedOptionsMap = mutableMapOf<String, String?>(
        "Music" to null,
        "Videos" to null,
        "Podcasts" to null,
        "Activities" to null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.user_prefrence)
        setupButtons()
        val btnNext = findViewById<Button>(R.id.btn_next)

        // Set up a click listener to navigate to PromptActivity
        btnNext.setOnClickListener {
                if (visiblity){
                    val intent = Intent(this, PromptActivity::class.java)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "Please select an option from each group", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun setupButtons() {
        // Music buttons
        setupButtonGroup("Music", listOf("Rock", "Jazz", "Classical", "Hip-Hop", "Country"))

        // Videos buttons
        setupButtonGroup("Videos", listOf("Action", "Comedy", "Sci-Fi", "Thriller", "Horror"))

        // Podcasts buttons
        setupButtonGroup("Podcasts", listOf("Technology", "Health", "Sports", "Business", "Education"))

        // Activities buttons
        setupButtonGroup("Activities", listOf("Gym", "Park", "Cafe", "Library", "Mall"))
    }

    private fun setupButtonGroup(group: String, options: List<String>) {
        options.forEach { option ->
            val buttonId = resources.getIdentifier("btn_${option.lowercase().replace("-", "_")}", "id", packageName)
            findViewById<Button>(buttonId)?.setOnClickListener { button ->
                toggleSelection(button as Button, group)
            }
        }
    }

    private fun toggleSelection(button: Button, group: String) {
        val option = button.text.toString()

        // If the option is already selected
        if (selectedOptionsMap[group] == option) {
            selectedOptionsMap[group] = null
            button.setBackgroundResource(R.drawable.rounded_button)
        } else {
            // Select the new option
            selectedOptionsMap[group] = option
            button.setBackgroundResource(R.drawable.button_selected)
        }
        checkAllGroupsSelected()
    }

    // Check if every group has a selected option and enable/disable the Next button
    private fun checkAllGroupsSelected() {
        val btnNext = findViewById<Button>(R.id.btn_next)

        // If all groups have at least one selected option, enable the Next button
        val allGroupsSelected = selectedOptionsMap.values.none { it == null }

        if (allGroupsSelected) {
            btnNext.visibility = View.VISIBLE
            btnNext.setBackgroundResource(R.color.Cheerly_Orange)
            // set the visibility to true
            visiblity = true
        }
        else {
            // change the color of the button
            btnNext.setBackgroundResource(R.color.grey)
            visiblity = false
        }


    }
}
