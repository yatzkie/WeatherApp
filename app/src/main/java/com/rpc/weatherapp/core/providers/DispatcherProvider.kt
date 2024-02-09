package com.rpc.weatherapp.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    fun main(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
}

class DispatcherProviderImpl: DispatcherProvider {
    override fun main() = Dispatchers.Main
    override fun io() = Dispatchers.IO

}