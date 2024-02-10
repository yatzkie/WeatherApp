package com.rpc.weatherapp.core.providers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.CancellationSignal
import androidx.core.content.ContextCompat

interface LocationProvider {
    fun getCurrentLocation(cancellationSignal: CancellationSignal, callback: LocationRequestCallback)
}

class LocationProviderImpl(private val context: Context): LocationProvider {
    override fun getCurrentLocation(cancellationSignal: CancellationSignal, callback: LocationRequestCallback) {
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPermission = coarsePermission && finePermission
        if (!hasPermission) {
            throw NoPermissionException()
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isLocationEnabled) {
            throw LocationDisabledException()
        }

        val hasNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!hasNetworkProvider) {
            throw NoAvailableProviderException()
        }

        val request = LocationRequest.Builder(1000)
            .build()

        locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, request, cancellationSignal, context.mainExecutor) { location ->
            if (location != null) {
                callback.onSuccess(location)
            } else {
                callback.onError(NoLocationFoundException())
            }
        }
    }

}

interface LocationRequestCallback {
    fun onSuccess(location: Location)
    fun onError(t: Throwable)
}

class NoPermissionException: IllegalStateException()
class LocationDisabledException: IllegalStateException()
class NoAvailableProviderException: IllegalStateException()
class NoLocationFoundException: IllegalStateException()
