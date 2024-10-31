package com.rajkumar.cheerly

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    object Success : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}