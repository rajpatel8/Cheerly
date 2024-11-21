package com.rajkumar.cheerly

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat

class UserPrefrence : ComponentActivity() {

    private var visibility = false

    // Track selected options for each group
    private val selectedOptionsMap = mutableMapOf<String, MutableList<String>>(
        "Music" to mutableListOf(),
        "Videos" to mutableListOf(),
        "Podcasts" to mutableListOf(),
        "Activities" to mutableListOf()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.user_prefrence)

        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_dark)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.Cheerly_Pink)

        setupButtons()
        setupNextButton()
    }

    @SuppressLint("MissingSuperCall") //we are not using super.onBackPressed() because it is invoking the default behavior of the back button not necessary in this case
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun setupNextButton() {
        val btnNext = findViewById<Button>(R.id.btn_next)
        btnNext.setBackgroundResource(R.color.grey)
        btnNext.setOnClickListener {
            if (visibility) {
                // Create EncryptedSharedPreferences
                val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

                // Write user preferences to EncryptedSharedPreferences
                val editor = sharedPreferences.edit()
                editor.putBoolean("isUserPreferenceSet", true)
                editor.putStringSet("selectedMusicOptions", selectedOptionsMap["Music"]?.toSet())
                editor.putStringSet("selectedVideoOptions", selectedOptionsMap["Videos"]?.toSet())
                editor.putStringSet("selectedPodcastOptions", selectedOptionsMap["Podcasts"]?.toSet())
                editor.putStringSet("selectedActivityOptions", selectedOptionsMap["Activities"]?.toSet())
                editor.apply()

                // Navigate to PromptActivity
                startActivity(Intent(this, PromptActivity::class.java))
            } else {
                Toast.makeText(this, "Please select an option from each group", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        setupButtonGroup("Music", listOf("Rock", "Jazz", "Classical", "Hip-Hop", "Country"))
        setupButtonGroup("Videos", listOf("Action", "Comedy", "Sci-Fi", "Thriller", "Horror"))
        setupButtonGroup("Podcasts", listOf("Technology", "Health", "Sports", "Business", "Education"))
        setupButtonGroup("Activities", listOf("Gym", "Park", "Cafe", "Library", "Mall"))
    }

    private fun setupButtonGroup(group: String, options: List<String>) {
        options.forEach { option ->
            val buttonId = resources.getIdentifier("btn_${option.lowercase().replace("-", "_")}", "id", packageName)
            findViewById<Button>(buttonId)?.setOnClickListener { button ->
                toggleSelection(button as Button, group, option)
            }
        }
    }

    private fun toggleSelection(button: Button, group: String, option: String) {
        val selectedOptions = selectedOptionsMap[group] ?: mutableListOf()
        if (selectedOptions.contains(option)) {
            selectedOptions.remove(option)
            button.setBackgroundResource(R.drawable.rounded_button)
        } else {
            selectedOptions.add(option)
            button.setBackgroundResource(R.drawable.button_selected)
        }
        selectedOptionsMap[group] = selectedOptions
        checkAllGroupsSelected()
    }

    private fun checkAllGroupsSelected() {
        val btnNext = findViewById<Button>(R.id.btn_next)
        val allGroupsSelected = selectedOptionsMap.values.all { it.isNotEmpty() }
        visibility = allGroupsSelected
//        btnNext.visibility = if (allGroupsSelected) View.VISIBLE else false

        btnNext.setBackgroundResource(if (allGroupsSelected) R.color.Cheerly_Orange else R.color.grey)
    }
}