package com.rpc.weatherapp.core.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rpc.weatherapp.core.domain.WeatherData

@Entity(tableName = "weather_data")
data class WeatherDataEntity(
    @PrimaryKey(autoGenerate = true)
    val uId: Long = 0,
    val temperature: Int,
    val location: String,
    val description: String,
    val icon: String,
    val userID: String,
) {
    fun toWeatherData(): WeatherData {
        return WeatherData(
            temperature = temperature,
            description = description,
            location = location,
            icon = icon
        )
    }
}