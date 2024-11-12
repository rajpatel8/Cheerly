package com.rajkumar.cheerly.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.rajkumar.cheerly.LoginActivity
import net.openid.appauth.*
import kotlin.coroutines.suspendCoroutine

class SpotifyAuthManager(private val context: Context) {
    companion object {
        private const val CLIENT_ID = "e4350e0b977f4e589509378a28cebffa"
        private const val REDIRECT_URI = "cheerly://callback"
        private const val AUTH_ENDPOINT = "https://accounts.spotify.com/authorize"
        private const val TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token"
        private const val SCOPE = "user-read-private user-read-email playlist-read-private streaming user-top-read user-read-recently-played "
    }

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(AUTH_ENDPOINT),
        Uri.parse(TOKEN_ENDPOINT)
    )

    private val authService = AuthorizationService(context)

    fun startAuth() {
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        ).setScope(SCOPE)

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        val authRequest = authRequestBuilder.build()

        val authIntent = authService.getAuthorizationRequestIntent(
            authRequest,
            customTabsIntent
        )

        if (context is LoginActivity) {
            context.startActivityForResult(authIntent, LoginActivity.RC_AUTH)
        }
    }

    suspend fun handleAuthResponse(response: AuthorizationResponse): TokenResponse =
        suspendCoroutine { continuation ->
            val tokenRequest = response.createTokenExchangeRequest()
            authService.performTokenRequest(tokenRequest) { tokenResponse, exception ->
                when {
                    tokenResponse != null -> continuation.resumeWith(Result.success(tokenResponse))
                    exception != null -> continuation.resumeWith(Result.failure(exception))
                    else -> continuation.resumeWith(Result.failure(Exception("Unknown error")))
                }
            }
        }

    fun dispose() {
        authService.dispose()
    }
}