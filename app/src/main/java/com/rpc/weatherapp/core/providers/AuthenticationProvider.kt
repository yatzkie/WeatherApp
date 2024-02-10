package com.rpc.weatherapp.core.providers

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest


interface AuthenticationProvider {
    fun getUser(): FirebaseUser?
    fun signInUser(email: String, password: String, signInCallback: SignInCallback)
    fun signUpUser(displayName: String, email: String, password: String, signUpCallback: SignUpCallback)
    fun signOutUser()
}

class AuthenticationProviderImpl(private val auth: FirebaseAuth): AuthenticationProvider {
    override fun getUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun signInUser(email: String, password: String, signInCallback: SignInCallback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.isCanceled) {
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

    override fun signUpUser(
        displayName: String,
        email: String,
        password: String,
        signUpCallback: SignUpCallback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .continueWithTask { task ->
                println("debugger listener: ${task.isSuccessful} | ${task.isCanceled} | ${task.isComplete} | ${task.exception?.message} > ${task.exception?.javaClass?.simpleName}")
                if (task.isSuccessful && !task.isCanceled) {
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    return@continueWithTask auth.currentUser?.updateProfile(profileChangeRequest)
                } else {
                    val newTask = TaskCompletionSource<Void>()
                    newTask.setException(task.exception ?: IllegalStateException("Unable to proceed with sign up due to unknown error."))
                    return@continueWithTask newTask.task
                }

            }
            .addOnCompleteListener { task ->
                println("debugger listener: ${task.isSuccessful} | ${task.isCanceled} | ${task.isComplete} | ${task.exception?.message} > ${task.exception?.javaClass?.simpleName}")
                if (task.isSuccessful && !task.isCanceled) {
                    signUpCallback.onSuccess()
                    return@addOnCompleteListener
                }
                val exception = task.exception
                if (exception == null) {
                    signUpCallback.onError(IllegalStateException("Failed to sign in due to unknown error."))
                }
                else if (exception !is FirebaseAuthException) {
                    signUpCallback.onError(IllegalStateException(exception.message))
                } else if (exception.errorCode.equals("ERROR_INVALID_CREDENTIAL", true)) {
                    //Do not let use now that account not does exist or if the password is incorrect
                    signUpCallback.onError(InvalidCredentialsException())
                } else {
                    signUpCallback.onError(IllegalStateException("Sign in failed due to error code: ${exception.errorCode}."))
                }
            }
    }

    override fun signOutUser() {
        auth.signOut()
    }
}


interface SignInCallback {
    fun onSuccess()
    fun onError(t: Throwable)
}

interface SignUpCallback {
    fun onSuccess()
    fun onError(t: Throwable)
}

class InvalidCredentialsException: IllegalStateException()