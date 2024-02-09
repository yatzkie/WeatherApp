package com.rpc.weatherapp.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.rpc.weatherapp.core.domain.User
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import com.rpc.weatherapp.core.providers.SignInCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface UserDataSource {

    suspend fun hasLoggedInUser(): Boolean
    suspend fun getLoggedInUser(): User
    suspend fun loginUser(email: String, password: String): Boolean
}

class UserDataSourceImpl(private val authProvider: AuthenticationProvider): UserDataSource {

    override suspend fun hasLoggedInUser(): Boolean {
        delay(1000)
        return authProvider.getUser() != null
    }
    override suspend fun getLoggedInUser(): User {
        return authProvider.getUser()?.let { currentUser ->
            User(currentUser.uid, currentUser.displayName ?: "Anonymous")
        } ?: throw NoLoggedInUserException("No Logged In User")
    }

    override suspend fun loginUser(email: String, password: String): Boolean {
        return suspendCoroutine { continuation ->
            authProvider.signInUser(email, password, signInCallback = object: SignInCallback {
                override fun onSuccess() {
                    continuation.resume(true)
                }

                override fun onError(t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}

class NoLoggedInUserException(override val message: String): RuntimeException(message)