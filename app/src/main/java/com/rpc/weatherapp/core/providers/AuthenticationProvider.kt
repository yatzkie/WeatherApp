package com.rpc.weatherapp.core.providers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rpc.weatherapp.core.domain.User


interface AuthenticationProvider {
    fun getUser(): FirebaseUser?
}

class AuthenticationProviderImpl(private val auth: FirebaseAuth): AuthenticationProvider {
    override fun getUser(): FirebaseUser? {
        return auth.currentUser
    }
}