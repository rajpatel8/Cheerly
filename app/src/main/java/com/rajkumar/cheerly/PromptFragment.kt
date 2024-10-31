package com.rajkumar.cheerly

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

class PromptFragment : Fragment() {
    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var subscribeButton: Button

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val premiumManager = PremiumManager(requireContext())

        // If user is already premium, show the actual prompt UI
        if (premiumManager.isPremiumUser()) {
            return createPromptUI()
        }

        // Otherwise show the premium subscription UI
        return createPremiumUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SubscriptionViewModel::class.java]

        viewModel.subscriptionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SubscriptionState.Loading -> {
                    loadingProgressBar.visibility = View.VISIBLE
                    subscribeButton.isEnabled = false
                }
                is SubscriptionState.Success -> {
                    loadingProgressBar.visibility = View.GONE
                    subscribeButton.isEnabled = true
                    // Replace the current fragment with the actual prompt UI
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, PromptFragment())
                        .commit()
                }
                is SubscriptionState.Error -> {
                    loadingProgressBar.visibility = View.GONE
                    subscribeButton.isEnabled = true
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createPromptUI(): View {
        val context = requireContext()
        val constraintLayout = ConstraintLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Create EditText for prompt input
        val promptInput = TextView(context).apply {
            id = View.generateViewId()
            hint = "How are you feeling today?"
            textSize = 18f
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(16)
                marginStart = dpToPx(16)
                marginEnd = dpToPx(16)
            }
            layoutParams = params
        }
        constraintLayout.addView(promptInput)

        return constraintLayout
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createPremiumUI(): View {
        val context = requireContext()
        val constraintLayout = ConstraintLayout(context).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Premium Icon
        val premiumIcon = TextView(context).apply {
            id = View.generateViewId()
            text = "🔒"
            textSize = 48f
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(64)
            }
            layoutParams = params
        }
        constraintLayout.addView(premiumIcon)

        // Title Text
        val titleText = TextView(context).apply {
            id = View.generateViewId()
            text = "Unlock Premium Features"
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black, null))
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = premiumIcon.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(24)
            }
            layoutParams = params
        }
        constraintLayout.addView(titleText)

        // Description Text
        val descriptionText = TextView(context).apply {
            id = View.generateViewId()
            text = "Get personalized AI-powered prompts to help you express your feelings more effectively. " +
                    "Premium members receive unlimited custom prompts tailored to their mood and situation."
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(dpToPx(32), 0, dpToPx(32), 0)
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = titleText.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(16)
            }
            layoutParams = params
        }
        constraintLayout.addView(descriptionText)

        // Price Text
        val priceText = TextView(context).apply {
            id = View.generateViewId()
            text = "Only $10/month"
            textSize = 20f
            setTextColor(resources.getColor(R.color.Cheerly_Orange, null))
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = descriptionText.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(24)
            }
            layoutParams = params
        }
        constraintLayout.addView(priceText)

        // Loading Progress Bar
        loadingProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleSmall).apply {
            id = View.generateViewId()
            visibility = View.GONE
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = priceText.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(16)
            }
            layoutParams = params
        }
        constraintLayout.addView(loadingProgressBar)

        // Subscribe Button
        subscribeButton = Button(context).apply {
            id = View.generateViewId()
            text = "Subscribe Now"
            setBackgroundResource(R.drawable.button_selected)
            setTextColor(resources.getColor(android.R.color.white, null))
            val params = ConstraintLayout.LayoutParams(
                dpToPx(200),
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = loadingProgressBar.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(16)
            }
            layoutParams = params
            setOnClickListener {
                viewModel.processSubscription(requireContext())
            }
        }
        constraintLayout.addView(subscribeButton)

        // Features List
        val featuresList = TextView(context).apply {
            id = View.generateViewId()
            text = "✓ Unlimited AI-powered prompts\n" +
                    "✓ Personalized suggestions\n" +
                    "✓ Advanced emotion analysis\n" +
                    "✓ Priority support"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black, null))
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = subscribeButton.id
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = dpToPx(32)
            }
            layoutParams = params
        }
        constraintLayout.addView(featuresList)

        return constraintLayout
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}