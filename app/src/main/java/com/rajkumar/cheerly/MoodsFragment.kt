package com.rajkumar.cheerly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class MoodsFragment : Fragment() {

    private val selectedOptions = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_moods, container, false)

        // Get button references and set click listeners
        val btnHappy: Button = view.findViewById(R.id.btnHappy)
        val btnSad: Button = view.findViewById(R.id.btnSad)
        val btnExcited: Button = view.findViewById(R.id.btnExcited)
        val btnRelaxed: Button = view.findViewById(R.id.btnRelaxed)

        // event listener for all buttons in one place
        val buttons = listOf(btnHappy, btnSad, btnExcited, btnRelaxed)
        buttons.forEach { button ->
            button.setOnClickListener {
                toggleSelection(button)
            }
        }

        return view
    }



    // handel toggle selection
    private fun toggleSelection(button: Button) {


        val option = button.text.toString()
        if (selectedOptions.contains(option)) {
            selectedOptions.remove(option)
            button.setBackgroundResource(R.drawable.rounded_button)
        } else {
            selectedOptions.add(option)
            button.setBackgroundResource(R.drawable.button_selected)
        }
    }

}

