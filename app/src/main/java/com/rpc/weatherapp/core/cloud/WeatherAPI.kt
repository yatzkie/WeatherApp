package com.rpc.weatherapp.core.cloud

import com.rpc.weatherapp.core.domain.CurrentWeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface WeatherAPI {

    @GET("/data/2.5/weather")
    fun getCurrentWeather(@QueryMap options: Map<String, String>): Call<CurrentWeatherResponse>
}