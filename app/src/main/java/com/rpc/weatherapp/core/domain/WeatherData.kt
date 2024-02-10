package com.rpc.weatherapp.core.domain

import com.rpc.weatherapp.core.local.WeatherDataEntity

data class WeatherData(
    val temperature: Int,
    val description: String,
    val icon: String,
    val location: String,
) {
    fun toEntity(uid: String): WeatherDataEntity {
        return WeatherDataEntity(
            userID = uid,
            temperature = temperature,
            description = description,
            icon = icon,
            location = location
        )
    }
}