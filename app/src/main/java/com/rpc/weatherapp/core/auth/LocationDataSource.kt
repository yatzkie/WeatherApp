package com.rpc.weatherapp.core.auth

import android.location.Location
import android.os.CancellationSignal
import com.rpc.weatherapp.core.providers.LocationProvider
import com.rpc.weatherapp.core.providers.LocationRequestCallback
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

interface LocationDataSource {
    fun getCurrentLocation(): Flow<Location>
}

class LocationDataSourceImpl(private val locationProvider: LocationProvider): LocationDataSource {
    override fun getCurrentLocation(): Flow<Location> {
        return callbackFlow {
            val cancellationSignal = CancellationSignal()
            locationProvider.getCurrentLocation(cancellationSignal, callback = object: LocationRequestCallback {
                override fun onSuccess(location: Location) {
                    launch { send(location) }
                }

                override fun onError(t: Throwable) {
                    throw t
                }

            })
            awaitClose {
                cancellationSignal.cancel()
                cancel()
            }
        }
    }

}