package com.rpc.weatherapp.core.domain

data class WeatherData(
    val temperature: Int,
    val description: String,
    val icon: String,
    val location: String,
)