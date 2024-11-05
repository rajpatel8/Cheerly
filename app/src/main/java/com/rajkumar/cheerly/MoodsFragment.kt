package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MoodsFragment : Fragment() {

    private var selectedButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_moods, container, false)

        // Get button references
        val btnHappy: Button = view.findViewById(R.id.btnHappy)
        val btnSad: Button = view.findViewById(R.id.btnSad)
        val btnExcited: Button = view.findViewById(R.id.btnExcited)
        val btnRelaxed: Button = view.findViewById(R.id.btnRelaxed)

        // Add all buttons to a list
        val buttons = listOf(btnHappy, btnSad, btnExcited, btnRelaxed)

        // Set click listener for each button
        buttons.forEach { button ->
            button.setOnClickListener {
                selectSingleOption(button)
            }
        }

        return view
    }

    private fun selectSingleOption(button: Button) {
        // Deselect the previously selected button, if any
        selectedButton?.setBackgroundResource(R.drawable.rounded_button)

        // Select the new button
        button.setBackgroundResource(R.drawable.button_selected)
        selectedButton = button

        // Get the mood text without emojis
        val mood = button.text.toString().split(" ")[0]

        try {
            val intent = Intent(context, MoodRecommendationActivity::class.java).apply {
                putExtra("selectedMood", mood)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}