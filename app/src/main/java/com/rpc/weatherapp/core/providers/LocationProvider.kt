package com.rpc.weatherapp.core.providers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import java.util.function.Consumer

interface LocationProvider {
    fun getCurrentLocation(cancellationSignal: CancellationSignal, callback: LocationRequestCallback)
}

class LocationProviderImpl(private val context: Context): LocationProvider {
    override fun getCurrentLocation(cancellationSignal: CancellationSignal, callback: LocationRequestCallback) {
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPermission = coarsePermission && finePermission
        if (!hasPermission) {
            callback.onError(NoPermissionException())
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isLocationEnabled) {
            callback.onError(LocationDisabledException())
            return
        }

        val hasNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!hasNetworkProvider) {
            callback.onError(NoAvailableProviderException())
            return
        }

        val request = LocationRequest.Builder(1000)
            .build()

        val consumer = Consumer<Location> { location ->
            callback.onSuccess(location)
        }

        locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, request, cancellationSignal, context.mainExecutor, consumer)
    }

}

interface LocationRequestCallback {
    fun onSuccess(location: Location)
    fun onError(t: Throwable)
}

class NoPermissionException: IllegalStateException()
class LocationDisabledException: IllegalStateException()
class NoAvailableProviderException: IllegalStateException()
