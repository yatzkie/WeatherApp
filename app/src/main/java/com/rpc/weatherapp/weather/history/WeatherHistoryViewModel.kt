package com.rpc.weatherapp.weather.history

import androidx.lifecycle.viewModelScope
import com.rpc.weatherapp.core.base.BaseViewModel
import com.rpc.weatherapp.core.domain.WeatherData
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.sources.UserDataSource
import com.rpc.weatherapp.core.sources.WeatherDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherHistoryViewModel(
    private val userDataSource: UserDataSource,
    private val weatherDataSource: WeatherDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : BaseViewModel<WeatherHistoryState, WeatherHistoryEvent>() {

    private val state = MutableStateFlow<WeatherHistoryState>(WeatherHistoryState.Idle)
    override fun sendEvent(event: WeatherHistoryEvent) {
        when(event) {
            WeatherHistoryEvent.FetchWeatherHistory -> fetchWeatherHistory()
        }
    }

    private fun fetchWeatherHistory() {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateState(WeatherHistoryState.Loading)
            try {
                val user = userDataSource.getLoggedInUser()
                val data  = weatherDataSource.getWeatherHistory(userId = user.uId)
                updateState(WeatherHistoryState.WeatherHistoryFetched(data))
            } catch (e: Exception) {
                updateState(WeatherHistoryState.Error(e.message ?: "Unable to fetch history. Unable to find user due to unknown error."))
            }
        }
    }

    override fun getState(): StateFlow<WeatherHistoryState> = state

    override fun updateState(newState: WeatherHistoryState) {
        state.value = newState
    }

}

sealed class WeatherHistoryState {
    object Idle: WeatherHistoryState()
    object Loading: WeatherHistoryState()
    data class WeatherHistoryFetched(val data: List<WeatherData>): WeatherHistoryState()
    data class Error(val message: String): WeatherHistoryState()
    object UnauthorizedUser: WeatherHistoryState()
}

sealed class WeatherHistoryEvent {
    object FetchWeatherHistory: WeatherHistoryEvent()
}