package com.rajkumar.cheerly.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.youtube.YouTubeScopes

class GoogleAuthManager(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(YouTubeScopes.YOUTUBE_READONLY),
                Scope(YouTubeScopes.YOUTUBE)
            )
            .requestId()
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent() = googleSignInClient.signInIntent

    fun getLastSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context)

    fun isSignedIn() = GoogleSignIn.getLastSignedInAccount(context) != null

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut()
            .addOnCompleteListener {
                onComplete()
            }
    }

    fun revokeAccess(onComplete: () -> Unit) {
        googleSignInClient.revokeAccess()
            .addOnCompleteListener {
                onComplete()
            }
    }
}