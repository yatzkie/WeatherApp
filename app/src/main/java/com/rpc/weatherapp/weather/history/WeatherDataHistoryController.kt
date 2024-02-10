package com.rpc.weatherapp.weather.history

import com.airbnb.epoxy.TypedEpoxyController
import com.rpc.weatherapp.core.domain.WeatherData

class WeatherDataHistoryController: TypedEpoxyController<List<WeatherData>>() {

    override fun buildModels(data: List<WeatherData>) {
        data.forEachIndexed { id, weatherData ->
            weatherData {
                id(id)
                icon(weatherData.icon)
                temperature(weatherData.temperature)
                description(weatherData.description)
                location(weatherData.location)
            }
        }
    }

}