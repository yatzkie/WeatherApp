package com.rpc.weatherapp.core

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.rpc.weatherapp.core.auth.UserDataSource
import com.rpc.weatherapp.core.auth.UserDataSourceImpl
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import com.rpc.weatherapp.core.providers.AuthenticationProviderImpl
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.providers.DispatcherProviderImpl
import com.rpc.weatherapp.splash.SplashViewModel
import com.rpc.weatherapp.user.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModules = module {
    single<AuthenticationProvider> { AuthenticationProviderImpl(Firebase.auth) }
    singleOf(::DispatcherProviderImpl) { bind<DispatcherProvider>() }
    singleOf(::UserDataSourceImpl) { bind<UserDataSource>() }
}

val viewModelModules = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
}
