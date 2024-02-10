package com.rpc.weatherapp.core

import androidx.room.Room
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rpc.weatherapp.core.sources.LocationDataSource
import com.rpc.weatherapp.core.sources.LocationDataSourceImpl
import com.rpc.weatherapp.core.sources.UserDataSource
import com.rpc.weatherapp.core.sources.UserDataSourceImpl
import com.rpc.weatherapp.core.sources.WeatherDataSource
import com.rpc.weatherapp.core.sources.WeatherDataSourceImpl
import com.rpc.weatherapp.core.cloud.RetrofitClient
import com.rpc.weatherapp.core.cloud.WeatherAPI
import com.rpc.weatherapp.core.local.AppDatabase
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import com.rpc.weatherapp.core.providers.AuthenticationProviderImpl
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.providers.DispatcherProviderImpl
import com.rpc.weatherapp.core.providers.LocationProvider
import com.rpc.weatherapp.core.providers.LocationProviderImpl
import com.rpc.weatherapp.core.providers.WeatherDataProvider
import com.rpc.weatherapp.core.providers.WeatherDataProviderImpl
import com.rpc.weatherapp.splash.SplashViewModel
import com.rpc.weatherapp.user.LoginViewModel
import com.rpc.weatherapp.user.SignUpViewModel
import com.rpc.weatherapp.weather.current.CurrentWeatherViewModel
import com.rpc.weatherapp.weather.history.WeatherHistoryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModules = module {
    single<Gson> {
        GsonBuilder()
            .setPrettyPrinting()
            .create()
    }
    single<AuthenticationProvider> { AuthenticationProviderImpl(Firebase.auth) }
    single<LocationProvider> { LocationProviderImpl(androidContext()) }
    single<WeatherDataProvider> {
        val retrofitClient = RetrofitClient.getInstance(get())
        val weatherAPI = retrofitClient.create(WeatherAPI::class.java)
        WeatherDataProviderImpl(weatherAPI)
    }

    single<AppDatabase> {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                Constant.DATABASE
             )
            .fallbackToDestructiveMigration()
            .build()
    }
    singleOf(::DispatcherProviderImpl) { bind<DispatcherProvider>() }
    singleOf(::UserDataSourceImpl) { bind<UserDataSource>() }
    singleOf(::LocationDataSourceImpl) { bind<LocationDataSource>() }
    singleOf(::WeatherDataSourceImpl) { bind<WeatherDataSource>() }
}

val viewModelModules = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::CurrentWeatherViewModel)
    viewModelOf(::WeatherHistoryViewModel)
}
