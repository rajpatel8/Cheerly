package com.rajkumar.cheerly

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar

class SubscriptionViewModel : ViewModel() {
    private val _subscriptionState = MutableLiveData<SubscriptionState>()
    val subscriptionState: LiveData<SubscriptionState> = _subscriptionState

    fun processSubscription(context: Context) {
        viewModelScope.launch {
            try {
                _subscriptionState.value = SubscriptionState.Loading

                // Simulated payment processing delay
                kotlinx.coroutines.delay(1500)

                // After successful payment
                val premiumManager = PremiumManager(context)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, 1) // Add one month

                premiumManager.apply {
                    setUserPremiumStatus(true)
                    setPremiumExpiryDate(calendar.timeInMillis)
                }

                _subscriptionState.value = SubscriptionState.Success
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Subscription failed")
            }
        }
    }
}