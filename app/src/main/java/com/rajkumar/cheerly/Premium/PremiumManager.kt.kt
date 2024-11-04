package com.rajkumar.cheerly.Premium

import android.content.Context
import android.content.SharedPreferences

class PremiumManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PremiumPrefs", Context.MODE_PRIVATE)

    fun isPremiumUser(): Boolean {
        return sharedPreferences.getBoolean("isPremium", false)
    }

    fun setUserPremiumStatus(isPremium: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean("isPremium", isPremium)
            apply()
        }
    }

    fun getPremiumExpiryDate(): Long {
        return sharedPreferences.getLong("premiumExpiry", 0)
    }

    fun setPremiumExpiryDate(expiryTimestamp: Long) {
        sharedPreferences.edit().apply {
            putLong("premiumExpiry", expiryTimestamp)
            apply()
        }
    }
}