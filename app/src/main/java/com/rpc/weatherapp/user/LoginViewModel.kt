package com.rpc.weatherapp.user

import androidx.lifecycle.viewModelScope
import com.rpc.weatherapp.core.auth.UserDataSource
import com.rpc.weatherapp.core.base.BaseViewModel
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.providers.InvalidCredentialsException
import com.rpc.weatherapp.core.utils.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userDataSource: UserDataSource, private val dispatcherProvider: DispatcherProvider): BaseViewModel<LoginState, LoginEvent>() {

    private val state = MutableStateFlow<LoginState>(LoginState.Idle)

    override fun sendEvent(event: LoginEvent) {
        when(event) {
            is LoginEvent.SignIn -> loginUser(event.email, event.password)
            LoginEvent.SignUp -> showSignUpPage()
        }
    }

    private fun showSignUpPage() {
        updateState(LoginState.SignUp)
    }

    override fun getState(): StateFlow<LoginState> = state

    private fun loginUser(email: String, password: String) {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateState(LoginState.Loading)
            if (!InputValidator.validateEmail(email)) {
                updateState(LoginState.InvalidEmailFormat)
                return@launch
            } else if (!InputValidator.validatePassword(password)) {
                updateState(LoginState.InvalidPasswordFormat)
                return@launch
            }
            try {
                userDataSource.loginUser(email, password)
                updateState(LoginState.SignInSuccessful)
            }
            catch (e: InvalidCredentialsException) {
                updateState(LoginState.InvalidCredentials)
            }
            catch (e: Exception) {
                updateState(
                    LoginState.Error(e.message ?: "Unable to complete request due to unknown reason.")
                )
            }
        }
    }

    override fun updateState(newState: LoginState) {
        this.state.value = newState
    }
}


sealed class LoginState {
    object Idle: LoginState()
    object Loading: LoginState()
    object SignInSuccessful: LoginState()
    object SignUp: LoginState()
    class Error(val message: String) : LoginState()

    object InvalidEmailFormat: LoginState()
    object InvalidPasswordFormat: LoginState()
    object InvalidCredentials: LoginState()

}

sealed class LoginEvent {
    data class SignIn(val email: String, val password: String): LoginEvent()
    object SignUp: LoginEvent()
}