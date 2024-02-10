package com.rpc.weatherapp.core.domain

import com.google.gson.annotations.SerializedName
import com.rpc.weatherapp.BuildConfig

data class CurrentWeatherResponse(
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: Main,
) {

    fun toWeatherData(): WeatherData? {
        if (weather.isEmpty()) {
            return null
        }
        val currentWeather = weather.first()
        return WeatherData(
            temperature = main.temperature.toInt(),
            description = currentWeather.main,
            icon = "${BuildConfig.RESOURCE_URL}/img/wn/${currentWeather.icon}@4x.png",
            location = cityName,
        )
    }
}

data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Main(
    @SerializedName("temp") val temperature: Double,
)