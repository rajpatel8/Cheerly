package com.rajkumar.cheerly

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


class PromptActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prompt)

        val btnMoods: Button = findViewById(R.id.btnMoods)
        val btnPrompt: Button = findViewById(R.id.btnPrompt)

        // Load default fragment (Moods Fragment)
        loadFragment(MoodsFragment())

        btnMoods.setOnClickListener {
            loadFragment(MoodsFragment())
        }

        btnPrompt.setOnClickListener {
            loadFragment(PromptFragment())
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
