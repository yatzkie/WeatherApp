package com.rpc.weatherapp.core.providers

import com.rpc.weatherapp.BuildConfig
import com.rpc.weatherapp.core.cloud.WeatherAPI
import com.rpc.weatherapp.core.cloud.CurrentWeatherResponse

interface WeatherDataProvider {
    fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse
}

class WeatherDataProviderImpl(private val weatherAPI: WeatherAPI): WeatherDataProvider {

    override fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse {
        if (BuildConfig.API_KEY == null) {
            throw InvalidAPIKeyException("Unable to use API due to missing API KEY")
        }
        val options = mapOf(
            "lat" to "$lat",
            "lon" to "$lon",
            "appid" to BuildConfig.API_KEY,
            "units" to "metric"
        )
        val response = weatherAPI.getCurrentWeather(options)
            .execute()
        if (response.isSuccessful) {
            return response.body() ?: throw NoResponseFoundException("No response to parse.")
        } else {
            throw RequestFailedException("Request failed: code: ${response.code()}")
        }
    }


}

class NoResponseFoundException(override val message: String?): IllegalStateException()
class RequestFailedException(override val message: String?): IllegalStateException(message)
class InvalidAPIKeyException(override val message: String?): IllegalStateException(message)
