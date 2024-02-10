package com.rpc.weatherapp.user

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rpc.weatherapp.R
import com.rpc.weatherapp.databinding.ActivitySignUpBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SignUpActivity: AppCompatActivity() {

    private var binding: ActivitySignUpBinding? = null
    private val viewModel: SignUpViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        bindingUserEvents()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getState().collect { state -> handleStates(state) }
            }
        }
    }

    private fun bindingUserEvents() {
        val bind = binding ?: return
        bind.signUpButton.setOnClickListener {
            resetToIdle()
            val displayName = bind.nameField.text.toString()
            val email = bind.emailField.text.toString()
            val password = bind.passwordField.text.toString()
            viewModel.sendEvent(SignUpEvent.SignUp(
                displayName = displayName,
                email = email,
                password = password
            ))
            hideSoftKeyBoard()
        }
        bind.signInLink.setOnClickListener {
            viewModel.sendEvent(SignUpEvent.SignIn)
        }
        bind.backButton.setOnClickListener {
            viewModel.sendEvent(SignUpEvent.GoBack)
        }
    }

    private fun handleStates(state: SignUpState) {
        when(state) {
            is SignUpState.Error -> showMessage(state.message)
            SignUpState.Idle -> resetToIdle()
            SignUpState.InvalidEmailFormat -> showMessage("Invalid Email format.")
            SignUpState.InvalidNameFormat -> showMessage("Invalid name format.")
            SignUpState.InvalidPasswordFormat -> showMessage("Password should be a minimum of 6 characters with contains a digit, special character (@#\\$%^&+=!), no whitespaces and uppercase, lowercase character.")
            SignUpState.Loading -> showLoading()
            SignUpState.SignIn -> gotoSignIn()
            SignUpState.SignUpSuccessful -> {
                gotoSignIn(signUpSuccessful = true)
            }
        }
    }

    private fun resetToIdle() {
        binding?.loadingIndicator?.isVisible = false
        binding?.notificationBanner?.isVisible = false
        binding?.bannerHint?.isVisible = false
    }

    private fun showLoading() {
        binding?.loadingIndicator?.isVisible = true
        binding?.notificationBanner?.isVisible = false
        binding?.bannerHint?.isVisible = false
    }

    private fun showMessage(message: String) {
        binding?.loadingIndicator?.isVisible = false
        binding?.notificationBanner?.isVisible = true
        binding?.notificationBanner?.text = message
        binding?.bannerHint?.isVisible = true
    }

    private fun gotoSignIn(signUpSuccessful: Boolean = false) {
        val result = if (signUpSuccessful) {
            Activity.RESULT_OK
        } else {
            Activity.RESULT_CANCELED
        }
        setResult(result)
        finish()
    }

    private fun hideSoftKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if(imm.isAcceptingText) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}