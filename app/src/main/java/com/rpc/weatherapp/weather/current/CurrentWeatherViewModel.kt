package com.rpc.weatherapp.weather.current

import androidx.lifecycle.viewModelScope
import com.rpc.weatherapp.core.sources.LocationDataSource
import com.rpc.weatherapp.core.sources.UserDataSource
import com.rpc.weatherapp.core.sources.WeatherDataSource
import com.rpc.weatherapp.core.base.BaseViewModel
import com.rpc.weatherapp.core.domain.User
import com.rpc.weatherapp.core.domain.WeatherData
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.providers.LocationDisabledException
import com.rpc.weatherapp.core.providers.NoAvailableProviderException
import com.rpc.weatherapp.core.providers.NoPermissionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class CurrentWeatherViewModel(
    private val locationDataSource: LocationDataSource,
    private val weatherDataSource: WeatherDataSource,
    private val userDataSource: UserDataSource,
    private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<CurrentWeatherState, CurrentWeatherEvent>() {

    private val state = MutableStateFlow<CurrentWeatherState>(CurrentWeatherState.Idle)
    private var currentUser: User? = null

    override fun sendEvent(event: CurrentWeatherEvent) {
        when(event) {
            CurrentWeatherEvent.FetchCurrentUser -> fetchCurrentUser()
            CurrentWeatherEvent.FetchCurrentWeather -> fetchCurrentWeather()
            is CurrentWeatherEvent.SaveCollectedData -> saveCollectedWeatherData(event.collectedData)
            CurrentWeatherEvent.SignOut -> signOutUser()
        }
    }

    private fun signOutUser() {
        viewModelScope.launch(dispatcherProvider.io()) {
            try {
                userDataSource.signOutUser()
                updateState(CurrentWeatherState.SignOut)
            } catch (e: Exception) {
                updateState(CurrentWeatherState.UnauthorizedUser)
            }
        }
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch(dispatcherProvider.io()) {
            try {
                var user = currentUser
                //Don't fetch anymore if current user is not null
                if(user != null) {
                    updateState(CurrentWeatherState.WelcomeUser(user.displayName))
                    return@launch
                }
                updateState(CurrentWeatherState.Loading)
                user = userDataSource.getLoggedInUser()
                updateState(CurrentWeatherState.WelcomeUser(user.displayName))
                currentUser = user
            } catch (e: Exception) {
                updateState(CurrentWeatherState.UnauthorizedUser)
            }
        }
    }

    private fun fetchCurrentWeather() {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateState(CurrentWeatherState.Loading)
            locationDataSource.getCurrentLocation()
                .flatMapLatest { location -> weatherDataSource.getCurrentWeather(location) }
                .catch { exception ->
                    when (exception) {
                        is UnknownHostException -> {
                            updateState(CurrentWeatherState.Error("Unable to connect to weather API. Please check your network connection."))
                        }
                        is NoPermissionException -> {
                            updateState(CurrentWeatherState.Error("Unable to fetch location due to location permission being denied."))
                        }
                        is LocationDisabledException -> {
                            updateState(CurrentWeatherState.Error("Location is currently disabled."))
                        }
                        is NoAvailableProviderException -> {
                            updateState(CurrentWeatherState.Error("No Location Providers available"))
                        }
                        else -> {
                            updateState(CurrentWeatherState.Error("Failed to fetch location data due to an unknown error."))
                        }
                    }
                }
                .collect { data ->
                    if (currentUser == null) {
                        updateState(CurrentWeatherState.UnauthorizedUser)
                        return@collect
                    }
                    sendEvent(CurrentWeatherEvent.SaveCollectedData(data))
                    updateState(CurrentWeatherState.LoadWeatherData(data))
                }
        }
    }

    private fun saveCollectedWeatherData(collectedData: WeatherData) {
        viewModelScope.launch(dispatcherProvider.io()) {
            try {
                val uid = currentUser?.uId
                if (uid == null) {
                    updateState(CurrentWeatherState.UnauthorizedUser)
                    return@launch
                }
                weatherDataSource.insertWeatherData(uid, collectedData)
            } catch (e: Exception) {
                updateState(CurrentWeatherState.Error(e.message ?: "Failed to save collected data due to an unknown reason."))
            }

        }
    }

    override fun getState(): StateFlow<CurrentWeatherState> = state

    override fun updateState(newState: CurrentWeatherState) {
        this.state.value = newState
    }
}

sealed class CurrentWeatherState {
    object Idle: CurrentWeatherState()
    object Loading: CurrentWeatherState()
    object UnauthorizedUser: CurrentWeatherState()
    data class WelcomeUser(val displayName: String): CurrentWeatherState()
    class LoadWeatherData(val data: WeatherData) : CurrentWeatherState()
    data class Error(val message: String): CurrentWeatherState()
    object SignOut: CurrentWeatherState()
}

sealed class CurrentWeatherEvent {
    data class SaveCollectedData(val collectedData: WeatherData) : CurrentWeatherEvent()

    object FetchCurrentUser: CurrentWeatherEvent()
    object FetchCurrentWeather: CurrentWeatherEvent()
    object SignOut : CurrentWeatherEvent()
}