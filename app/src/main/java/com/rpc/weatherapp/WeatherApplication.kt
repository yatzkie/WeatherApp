package com.rpc.weatherapp

import android.app.Application
import com.rpc.weatherapp.core.appModules
import com.rpc.weatherapp.core.coreModules
import com.rpc.weatherapp.core.viewModelModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class WeatherApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@WeatherApplication)
            modules(coreModules, viewModelModules)
        }
    }
}