package com.rpc.weatherapp.providers

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.rpc.weatherapp.core.providers.LocationDisabledException
import com.rpc.weatherapp.core.providers.LocationProvider
import com.rpc.weatherapp.core.providers.LocationProviderImpl
import com.rpc.weatherapp.core.providers.LocationRequestCallback
import com.rpc.weatherapp.core.providers.NoAvailableProviderException
import com.rpc.weatherapp.core.providers.NoPermissionException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor
import java.util.function.Consumer

class LocationProviderTests {

    private lateinit var providerInTest: LocationProvider

    @MockK
    private lateinit var context: Context

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        providerInTest = LocationProviderImpl(context)
    }

    @Test
    fun `Should throw exception when app does not have location permission`() {
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        val cancellationSignal = CancellationSignal()
        val callback = mockk<LocationRequestCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.getCurrentLocation(cancellationSignal, callback)

        verify { callback.onError(withArg { assertTrue(it is NoPermissionException) }) }
    }

    @Test
    fun `Should throw exception when location is disabled`() {
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        val locationManager = mockk<LocationManager> {
            every { isLocationEnabled } returns false
        }
        every { context.getSystemService(any()) } returns locationManager

        val cancellationSignal = CancellationSignal()
        val callback = mockk<LocationRequestCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.getCurrentLocation(cancellationSignal, callback)

        verify { callback.onError(withArg { assertTrue(it is LocationDisabledException) }) }
    }

    @Test
    fun `Should throw exception when device does not have network provider`() {
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        val locationManager = mockk<LocationManager> {
            every { isLocationEnabled } returns true
            every { isProviderEnabled(any()) } returns false
        }
        every { context.getSystemService(any()) } returns locationManager

        val cancellationSignal = CancellationSignal()
        val callback = mockk<LocationRequestCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.getCurrentLocation(cancellationSignal, callback)

        verify { callback.onError(withArg { assertTrue(it is NoAvailableProviderException) }) }
    }

    @Test
    fun `Should call onSuccess when location manager returns a location`() {
        val expectedLocation = mockk<Location>()
        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        val expectedRequest = mockk<LocationRequest>()
        mockkConstructor(LocationRequest.Builder::class)
        every { anyConstructed<LocationRequest.Builder>().build() } returns expectedRequest

        val expectedExecutor = mockk<Executor>()
        every { context.mainExecutor } returns expectedExecutor

        val consumerSlot = slot<Consumer<Location>>()
        val locationManager = mockk<LocationManager> {
            every { isLocationEnabled } returns true
            every { isProviderEnabled(any()) } returns true
            every { getCurrentLocation(any(), any(), any(), any(), capture(consumerSlot)) } answers {
                consumerSlot.captured.accept(expectedLocation)
            }

        }
        every { context.getSystemService(any()) } returns locationManager

        val cancellationSignal = CancellationSignal()
        val callback = mockk<LocationRequestCallback> {
            every { onSuccess(any()) } just runs
        }
        providerInTest.getCurrentLocation(cancellationSignal, callback)
        consumerSlot.captured.accept(expectedLocation)

        verify { callback.onSuccess(expectedLocation) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}