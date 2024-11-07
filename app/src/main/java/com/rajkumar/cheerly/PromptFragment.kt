package com.rajkumar.cheerly

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.rajkumar.cheerly.Premium.SubscriptionState
import com.rajkumar.cheerly.Premium.SubscriptionViewModel
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PromptFragment : Fragment() {
    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var subscribeButton: Button
    private lateinit var subscriptionLayout: LinearLayout
    private lateinit var messageTextView: TextView

    companion object {
        private const val PREFS_NAME = "subscription_prefs"
        private const val SUBSCRIPTION_DATE_KEY = "subscription_date"
        private val ONE_MONTH_MILLIS = TimeUnit.DAYS.toMillis(30) // 30 days in milliseconds
        private const val TAG = "PromptFragment"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prompt, container, false)

        // Initialize UI components
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        subscribeButton = view.findViewById(R.id.subscribeButton)
        subscriptionLayout = view.findViewById(R.id.subscriptionLayout)
        messageTextView = view.findViewById(R.id.messageTextView)

        messageTextView.visibility = View.GONE // Ensure it's initially hidden

        // Check if a month has passed since the last subscription
        if (isSubscribed()) {
            Log.d(TAG, "User is already subscribed.")
            showSubscriptionMessage("Welcome! You have successfully subscribed!!")
        } else {
            viewModel = ViewModelProvider(this).get(SubscriptionViewModel::class.java)
            subscribeButton.setOnClickListener {
                subscribeUser()
            }
            observeSubscriptionState()
        }

        return view
    }

    private fun observeSubscriptionState() {
        viewModel.subscriptionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SubscriptionState.Loading -> {
                    Log.d(TAG, "Subscription is loading...")
                    loadingProgressBar.visibility = View.VISIBLE
                    subscribeButton.isEnabled = false
                }
                is SubscriptionState.Success -> {
                    Log.d(TAG, "Subscription succeeded.")
                    loadingProgressBar.visibility = View.GONE
                    saveSubscriptionDate()
                    showSubscriptionMessage("You have subscribed successfully!")
                }
                is SubscriptionState.Error -> {
                    Log.d(TAG, "Subscription failed: ${state.message}")
                    loadingProgressBar.visibility = View.GONE
                    subscribeButton.isEnabled = true
                    Snackbar.make(requireView(), "Subscription failed: ${state.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun subscribeUser() {
        Log.d(TAG, "User clicked subscribe.")
        loadingProgressBar.visibility = View.VISIBLE
        subscribeButton.isEnabled = false

        // Show loading for 1 second before processing the subscription
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.processSubscription(requireContext())
        }, 1000)
    }

    private fun showSubscriptionMessage(message: String) {
        Log.d(TAG, "Showing subscription message: $message")

        // Hide subscription UI and show the message
        subscriptionLayout.visibility = View.GONE
        messageTextView.text = message
        messageTextView.visibility = View.VISIBLE
    }

    private fun saveSubscriptionDate() {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putLong(SUBSCRIPTION_DATE_KEY, Calendar.getInstance().timeInMillis)
        editor.apply()
    }

    private fun isSubscribed(): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSubscriptionDate = sharedPrefs.getLong(SUBSCRIPTION_DATE_KEY, 0)
        val currentTime = Calendar.getInstance().timeInMillis
        return (currentTime - lastSubscriptionDate) < ONE_MONTH_MILLIS
    }
}
