package com.rpc.weatherapp.user

import com.rpc.weatherapp.core.auth.UserDataSource
import com.rpc.weatherapp.core.providers.DispatcherProvider
import com.rpc.weatherapp.core.providers.InvalidCredentialsException
import com.rpc.weatherapp.rules.MainCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTests {

    @JvmField
    @ExperimentalCoroutinesApi
    @Rule
    var mainCoroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var userDataSource: UserDataSource

    private lateinit var vmInTest: LoginViewModel

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val dispatcherProvider = mockk<DispatcherProvider> {
            every { main() } returns UnconfinedTestDispatcher()
            every { io() } returns UnconfinedTestDispatcher()
        }
        vmInTest = LoginViewModel(userDataSource, dispatcherProvider)
    }

    @Test
    fun `Should show error when email entered is invalid`() {
        val initialState = vmInTest.getState().value
        assertTrue(initialState is LoginState.Idle)

        vmInTest.sendEvent(LoginEvent.SignIn("invalid@email", "password"))

        val currentState = vmInTest.getState().value
        assertTrue(currentState is LoginState.InvalidEmailFormat)
    }

    @Test
    fun `Should show error when password entered is invalid`() {
        val initialState = vmInTest.getState().value
        assertTrue(initialState is LoginState.Idle)

        vmInTest.sendEvent(LoginEvent.SignIn("valid@email.com", "password"))

        val currentState = vmInTest.getState().value
        assertTrue(currentState is LoginState.InvalidPasswordFormat)
    }

    @Test
    fun `Should show error when credentials are invalid`() {
        val initialState = vmInTest.getState().value
        assertTrue(initialState is LoginState.Idle)

        val validEmail = "valid@email.com"
        val validPassword = "P@ssw0rd!"
        coEvery { userDataSource.loginUser(any(), any()) } throws InvalidCredentialsException()

        vmInTest.sendEvent(LoginEvent.SignIn(validEmail, validPassword))

        val currentState = vmInTest.getState().value
        assertTrue(currentState is LoginState.InvalidCredentials)

        coVerify { userDataSource.loginUser(validEmail, validPassword) }
    }

    @Test
    fun `Should show error when an exception occurs when trying to sign in`() {
        val initialState = vmInTest.getState().value
        assertTrue(initialState is LoginState.Idle)

        val validEmail = "valid@email.com"
        val validPassword = "P@ssw0rd!"
        coEvery { userDataSource.loginUser(any(), any()) } throws IllegalStateException("Mock test message")

        vmInTest.sendEvent(LoginEvent.SignIn(validEmail, validPassword))

        val currentState = vmInTest.getState().value
        assertTrue(currentState is LoginState.Error)
        assertEquals("Mock test message", (currentState as LoginState.Error).message)

        coVerify { userDataSource.loginUser(validEmail, validPassword) }
    }

    @Test
    fun `Should redirect to sign up screen when sign up event is triggered`() {
        val initialState = vmInTest.getState().value
        assertTrue(initialState is LoginState.Idle)

        vmInTest.sendEvent(LoginEvent.SignUp)

        val currentState = vmInTest.getState().value
        assertTrue(currentState is LoginState.SignUp)

        vmInTest.sendEvent(LoginEvent.AwaitingSignUpResult)

        val finalState = vmInTest.getState().value
        assertTrue(finalState is LoginState.Idle)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}