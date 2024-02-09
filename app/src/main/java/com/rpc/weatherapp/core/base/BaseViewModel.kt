package com.rpc.weatherapp.core.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<S, E>: ViewModel() {

    abstract fun sendEvent(event: E)
    abstract fun getState(): StateFlow<S>
    protected abstract fun updateState(newState: S)

}