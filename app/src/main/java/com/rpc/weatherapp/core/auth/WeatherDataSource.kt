package com.rpc.weatherapp.core.auth

import android.location.Location
import com.rpc.weatherapp.core.domain.WeatherData
import com.rpc.weatherapp.core.providers.WeatherDataProvider
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

interface WeatherDataSource {
    fun getCurrentWeather(location: Location): Flow<WeatherData>
    fun insertWeatherData(uid: String, collectedData: WeatherData)

}

class WeatherDataSourceImpl(private val weatherDataProvider: WeatherDataProvider): WeatherDataSource {
    override fun getCurrentWeather(location: Location): Flow<WeatherData> {
        return callbackFlow {
            val response = weatherDataProvider.getCurrentWeather(location.latitude, location.longitude)
            val weatherData = response.toWeatherData() ?: throw NoWeatherDataException("No weather data found.")
            launch { send(weatherData) }
            awaitClose {
                cancel()
            }
        }
    }

    override fun insertWeatherData(uid: String, collectedData: WeatherData) {
        //Room DB
    }

}

class NoWeatherDataException(override val message: String?): IllegalStateException(message)