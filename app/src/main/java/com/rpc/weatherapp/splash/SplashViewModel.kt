package com.rpc.weatherapp.splash

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpc.weatherapp.core.auth.UserDataSource
import com.rpc.weatherapp.core.base.BaseViewModel
import com.rpc.weatherapp.core.providers.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(private val userDataSource: UserDataSource, private val dispatcherProvider: DispatcherProvider): BaseViewModel<SplashState, SplashEvent>(), DefaultLifecycleObserver {

    private val state = MutableStateFlow<SplashState>(SplashState.Loading)

    override fun sendEvent(event: SplashEvent) {
        when(event) {
            SplashEvent.FetchLoggedInUser -> hasLoggedInUser()
        }
    }

    override fun getState(): StateFlow<SplashState> = state

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        sendEvent(SplashEvent.FetchLoggedInUser)
    }

    private fun hasLoggedInUser() {
        viewModelScope.launch(dispatcherProvider.io()) {
            state.value = when(userDataSource.hasLoggedInUser()) {
                true -> SplashState.Authenticated
                false -> SplashState.Unauthorized
            }
        }
    }

}

sealed class SplashState {
    object Loading: SplashState()
    object Authenticated: SplashState()
    object Unauthorized: SplashState()
}

sealed class SplashEvent {
    object FetchLoggedInUser: SplashEvent()
}