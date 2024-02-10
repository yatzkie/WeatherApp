package com.rpc.weatherapp.core.sources

import android.location.Location
import com.rpc.weatherapp.core.domain.WeatherData
import com.rpc.weatherapp.core.local.AppDatabase
import com.rpc.weatherapp.core.providers.WeatherDataProvider
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

interface WeatherDataSource {
    fun getCurrentWeather(location: Location): Flow<WeatherData>
    suspend fun insertWeatherData(uid: String, collectedData: WeatherData)
    suspend fun getWeatherHistory(userId: String): List<WeatherData>

}

class WeatherDataSourceImpl(private val weatherDataProvider: WeatherDataProvider, private val database: AppDatabase): WeatherDataSource {
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

    override suspend fun insertWeatherData(uid: String, collectedData: WeatherData) {
        database.weatherDataDao().insertWeatherData(collectedData.toEntity(uid))
    }

    override suspend fun getWeatherHistory(userId: String): List<WeatherData> {
        val result = database.weatherDataDao().getWeatherData(userId)
        return result.map { entity -> entity.toWeatherData() }
    }

}

class NoWeatherDataException(override val message: String?): IllegalStateException(message)