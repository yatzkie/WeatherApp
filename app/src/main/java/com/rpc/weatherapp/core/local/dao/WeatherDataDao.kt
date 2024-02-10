package com.rpc.weatherapp.core.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rpc.weatherapp.core.local.WeatherDataEntity

@Dao
interface WeatherDataDao {

    @Insert
    suspend fun insertWeatherData(weatherData: WeatherDataEntity)

    @Query("SELECT * FROM weather_data WHERE userId = :userId ORDER BY uId DESC")
    suspend fun getWeatherData(userId: String): List<WeatherDataEntity>

}