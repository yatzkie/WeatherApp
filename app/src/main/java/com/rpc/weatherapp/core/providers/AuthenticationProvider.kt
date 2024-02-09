package com.rpc.weatherapp.core.providers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.rpc.weatherapp.core.domain.User
import java.lang.Exception
import kotlin.math.sign


interface AuthenticationProvider {
    fun getUser(): FirebaseUser?

    fun signInUser(email: String, password: String, signInCallback: SignInCallback)
}

class AuthenticationProviderImpl(private val auth: FirebaseAuth): AuthenticationProvider {
    override fun getUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun signInUser(email: String, password: String, signInCallback: SignInCallback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signInCallback.onSuccess()
                    return@addOnCompleteListener
                }
                val exception = task.exception
                if (exception == null) {
                    signInCallback.onError(IllegalStateException("Failed to sign in due to unknown error."))
                }
                else if (exception !is FirebaseAuthException) {
                    signInCallback.onError(IllegalStateException(exception.message))
                } else if (exception.errorCode.equals("ERROR_INVALID_CREDENTIAL", true)) {
                    //Do not let use now that account not does exist or if the password is incorrect
                    signInCallback.onError(InvalidCredentialsException())
                } else {
                    signInCallback.onError(IllegalStateException("Sign in failed due to error code: ${exception.errorCode}."))
                }
            }

    }
}


interface SignInCallback {
    fun onSuccess()
    fun onError(t: Throwable)
}

class InvalidCredentialsException: IllegalStateException()