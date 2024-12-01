package com.rajkumar.cheerly

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class PromptActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prompt)

        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_dark)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.Cheerly_Pink)

        val btnMoods: Button = findViewById(R.id.btnMoods)
        val btnPrompt: Button = findViewById(R.id.btnPrompt)

        // Load default fragment (Moods Fragment)
        loadFragment(MoodsFragment())
        updateUI(MoodsFragment(), btnMoods, btnPrompt)

        btnMoods.setOnClickListener {
            updateUI(MoodsFragment(), btnMoods, btnPrompt)
        }

        btnPrompt.setOnClickListener {
            updateUI(PromptFragment(), btnPrompt, btnMoods)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateUI(fragment: Fragment, selectedButton: Button, unselectedButton: Button) {
        loadFragment(fragment)
        selectedButton.setBackgroundResource(R.drawable.button_selected)
        unselectedButton.setBackgroundResource(R.drawable.rounded_button)
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        unselectedButton.setTextColor(ContextCompat.getColor(this, R.color.black))
    }
}