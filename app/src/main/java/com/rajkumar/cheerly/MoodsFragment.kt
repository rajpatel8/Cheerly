package com.rajkumar.cheerly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class MoodsFragment : Fragment() {

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

        btnHappy.setOnClickListener {
            Toast.makeText(requireContext(), "Mood: Happy", Toast.LENGTH_SHORT).show()
        }

        btnSad.setOnClickListener {
            Toast.makeText(requireContext(), "Mood: Sad", Toast.LENGTH_SHORT).show()
        }

        btnExcited.setOnClickListener {
            Toast.makeText(requireContext(), "Mood: Excited", Toast.LENGTH_SHORT).show()
        }

        btnRelaxed.setOnClickListener {
            Toast.makeText(requireContext(), "Mood: Relaxed", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
