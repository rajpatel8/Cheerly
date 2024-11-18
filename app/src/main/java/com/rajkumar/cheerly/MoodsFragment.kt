package com.rajkumar.cheerly

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class MoodsFragment : Fragment() {
    private var selectedCard: MaterialCardView? = null
    private lateinit var moodButtons: List<Pair<MaterialCardView, Button>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_moods, container, false)

        // Initialize buttons and cards
        moodButtons = listOf(
            createMoodPair(view, R.id.btnHappy),
            createMoodPair(view, R.id.btnSad),
            createMoodPair(view, R.id.btnExcited),
            createMoodPair(view, R.id.btnRelaxed),
            createMoodPair(view, R.id.btnBored),
            createMoodPair(view, R.id.btnAnxious),
            createMoodPair(view, R.id.btnFocused)
        )

        // Set up click listeners
        moodButtons.forEach { (card, button) ->
            // Enable button clicks
            button.setOnClickListener {
                handleSelection(card, button)
            }

            // Also enable card clicks
            card.setOnClickListener {
                handleSelection(card, button)
            }
        }

        return view
    }

    private fun createMoodPair(view: View, buttonId: Int): Pair<MaterialCardView, Button> {
        val button = view.findViewById<Button>(buttonId)
        val card = button.parent as MaterialCardView
        return Pair(card, button)
    }

    override fun onResume() {
        super.onResume()
        resetAllCards()
    }

    private fun handleSelection(card: MaterialCardView, button: Button) {
        // First reset previous selection
        selectedCard?.setCardBackgroundColor(resources.getColor(android.R.color.white))

        // Animate and select new card
        animateCard(card) {
            // Set new selection
            card.setCardBackgroundColor(resources.getColor(R.color.Cheerly_Orange))
            selectedCard = card

            // Navigate after animation
            val mood = button.text.toString().split(" ")[0]
            navigateToRecommendations(mood)
        }
    }

    private fun animateCard(card: MaterialCardView, onComplete: () -> Unit) {
        card.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                card.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        onComplete()
                    }
            }
            .start()
    }

    private fun resetAllCards() {
        moodButtons.forEach { (card, _) ->
            card.setCardBackgroundColor(resources.getColor(android.R.color.white))
            card.scaleX = 1f
            card.scaleY = 1f
        }
        selectedCard = null
    }

    private fun navigateToRecommendations(mood: String) {
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