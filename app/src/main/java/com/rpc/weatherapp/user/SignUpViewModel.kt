package com.rpc.weatherapp.user

import androidx.lifecycle.viewModelScope
import com.rpc.weatherapp.core.sources.UserDataSource
import com.rpc.weatherapp.core.base.BaseViewModel
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.utils.InputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(private val userDataSource: UserDataSource, private val dispatcherProvider: DispatcherProvider): BaseViewModel<SignUpState, SignUpEvent>() {

    private val state = MutableStateFlow<SignUpState>(SignUpState.Idle)
    override fun sendEvent(event: SignUpEvent) {
        when(event) {
            SignUpEvent.GoBack,
            SignUpEvent.SignIn -> goToSignIn()
            is SignUpEvent.SignUp -> signUp(event.displayName, event.email, event.password)
            SignUpEvent.SignUpSuccessful -> updateState(SignUpState.SignUpSuccessful)
        }
    }

    private fun signUp(displayName: String, email: String, password: String) {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateState(SignUpState.Loading)
            if (!InputValidator.validateName(displayName)) {
                updateState(SignUpState.InvalidNameFormat)
                return@launch
            }
            else if (!InputValidator.validateEmail(email)) {
                updateState(SignUpState.InvalidEmailFormat)
                return@launch
            } else if (!InputValidator.validatePassword(password)) {
                updateState(SignUpState.InvalidPasswordFormat)
                return@launch
            }
            try {
                userDataSource.signUpUser(displayName, email, password)
                updateState(SignUpState.SignUpSuccessful)
            } catch (e: Exception) {
                updateState(SignUpState.Error(e.message ?: "Sign up failed due to an unknown error."))
            }
        }
    }

    private fun goToSignIn() {
        updateState(SignUpState.SignIn)
    }

    override fun getState(): StateFlow<SignUpState> = state

    override fun updateState(newState: SignUpState) {
        this.state.value = newState
    }

}


sealed class SignUpState {
    object Idle: SignUpState()
    object Loading: SignUpState()
    object InvalidNameFormat: SignUpState()
    object InvalidEmailFormat: SignUpState()
    object InvalidPasswordFormat: SignUpState()

    data class Error(val message: String): SignUpState()
    object SignIn: SignUpState()
    object SignUpSuccessful: SignUpState()
}

sealed class SignUpEvent {
    data class SignUp(val displayName: String, val email: String, val password: String): SignUpEvent()
    object SignIn: SignUpEvent()
    object GoBack: SignUpEvent()
    object SignUpSuccessful: SignUpEvent()
}