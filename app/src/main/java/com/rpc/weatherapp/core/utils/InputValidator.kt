package com.rpc.weatherapp.core.utils

import android.util.Patterns
import androidx.core.util.PatternsCompat
import java.util.regex.Pattern

object InputValidator {
    fun validateEmail(email: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$")
        return pattern.matcher(password).matches()
    }

    fun validateName(name: String): Boolean {
        val pattern = Pattern.compile("^(?=.{1,40}$)[a-zA-Z]+(?:[-'\\s][a-zA-Z]+)*$")
        return pattern.matcher(name).matches()
    }

}