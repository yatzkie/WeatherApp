package com.rpc.weatherapp.user

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rpc.weatherapp.R
import com.rpc.weatherapp.databinding.ActivityLoginBinding
import com.rpc.weatherapp.weather.MainActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity: AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    private val viewModel: LoginViewModel by inject()
    private val signUpContract = registerForActivityResult(SignUpContract()) { signUpSuccessful ->
        if (signUpSuccessful) goToMain()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        bindUserEvents()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getState().collect { state -> handleStates(state) }
            }
        }
    }

    private fun bindUserEvents() {
        val bind = binding ?: return

        bind.signInButton.setOnClickListener {
            resetToIdle()
            val email = bind.emailField.text.toString()
            val password = bind.passwordField.text.toString()
            viewModel.sendEvent(LoginEvent.SignIn(email, password))
            hideSoftKeyBoard()
        }

        bind.signUpLink.setOnClickListener {
            viewModel.sendEvent(LoginEvent.SignUp)
        }
    }

    private fun handleStates(state: LoginState) {
        when(state) {
            LoginState.Idle -> resetToIdle()
            LoginState.Loading -> showLoading()
            is LoginState.Error -> showMessage(state.message, isError = true)
            LoginState.SignInSuccessful -> goToMain()
            LoginState.SignUp -> goToSignUp()
            LoginState.InvalidCredentials -> showMessage("Incorrect email or password.", isError = true)
            LoginState.InvalidEmailFormat -> showMessage("Email entered is not a valid email.", isError = true)
            LoginState.InvalidPasswordFormat ->
                showMessage("Password should be a minimum of 6 characters with contains a digit, special character (@#\\$%^&+=!), no whitespaces and uppercase, lowercase character.", isError = true)
        }
    }

    private fun resetToIdle() {
        binding?.notificationBanner?.isVisible = false
        binding?.loadingIndicator?.isVisible = false
        binding?.bannerHint?.isVisible = false
    }

    private fun showMessage(message: String, isError: Boolean = false) {
        binding?.notificationBanner?.isVisible = true
        binding?.notificationBanner?.text = message
        binding?.loadingIndicator?.isVisible = false
        binding?.bannerHint?.isVisible = true
        binding?.notificationBanner?.setBackgroundResource(if (isError) {
            R.drawable.rounded_error_notification
        } else {
            R.drawable.rounded_notification
        })
    }

    private fun showLoading() {
        binding?.loadingIndicator?.isVisible = true
    }
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToSignUp() {
        viewModel.sendEvent(LoginEvent.AwaitingSignUpResult)
        signUpContract.launch(Unit)
    }


    private fun hideSoftKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if(imm.isAcceptingText) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}