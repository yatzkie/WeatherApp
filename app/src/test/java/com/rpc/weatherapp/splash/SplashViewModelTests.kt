package com.rpc.weatherapp.splash

import com.rpc.weatherapp.core.auth.UserDataSource
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.rules.MainCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SplashViewModelTests {

    private lateinit var vmInTest: SplashViewModel

    @JvmField
    @ExperimentalCoroutinesApi
    @Rule
    var mainCoroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var userDataSource: UserDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val dispatcherProvider = mockk<DispatcherProvider> {
            every { main() } returns UnconfinedTestDispatcher()
            every { io() } returns UnconfinedTestDispatcher()
        }
        vmInTest = SplashViewModel(userDataSource, dispatcherProvider)
    }

    @Test
    fun `Should return unauthorized state when there is no logged in user`() {
        val initialValue = vmInTest.getStates().value
        assertTrue(initialValue is SplashState.Loading)

        coEvery { userDataSource.hasLoggedInUser() } returns false

        vmInTest.sendEvent(SplashEvent.FetchLoggedInUser)

        val currentValue = vmInTest.getStates().value
        assertTrue(currentValue is SplashState.Unauthorized)

        coVerify { userDataSource.hasLoggedInUser() }

    }

    @Test
    fun `Should return unauthorized state when there is a logged in user`() {
        val initialValue = vmInTest.getStates().value
        assertTrue(initialValue is SplashState.Loading)

        coEvery { userDataSource.hasLoggedInUser() } returns true

        vmInTest.sendEvent(SplashEvent.FetchLoggedInUser)

        val currentValue = vmInTest.getStates().value
        assertTrue(currentValue is SplashState.Authenticated)

        coVerify { userDataSource.hasLoggedInUser() }

    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}