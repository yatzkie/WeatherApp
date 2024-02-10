package com.rpc.weatherapp.sources

import com.google.firebase.auth.FirebaseUser
import com.rpc.weatherapp.core.sources.UserDataSource
import com.rpc.weatherapp.core.sources.UserDataSourceImpl
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import com.rpc.weatherapp.core.providers.SignInCallback
import com.rpc.weatherapp.core.providers.SignUpCallback
import com.rpc.weatherapp.core.sources.NoLoggedInUserException
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class UserDataSourcesTests {

    private lateinit var sourceInTest: UserDataSource

    @MockK
    private lateinit var provider: AuthenticationProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sourceInTest = UserDataSourceImpl(provider)
    }

    @Test
    fun `Should return false if there is no logged in user`() = runTest {
        coEvery { provider.getUser() } returns null
        val hasLoggedInUser = sourceInTest.hasLoggedInUser()
        assertFalse(hasLoggedInUser)
    }

    @Test
    fun `Should return false if there is a logged in user`() = runTest {
        coEvery { provider.getUser() } returns mockk()
        val hasLoggedInUser = sourceInTest.hasLoggedInUser()
        assertTrue(hasLoggedInUser)
    }

    @Test
    fun `Should generate correct user object based on provider's return`() = runTest {
        val expectedUser = mockk<FirebaseUser> {
            every { uid } returns "user-id"
            every { displayName } returns "Display Name"
        }

        every { provider.getUser() } returns expectedUser

        val actual = sourceInTest.getLoggedInUser()

        verify { provider.getUser() }

        assertEquals(expectedUser.uid, actual.uId)
        assertEquals(expectedUser.displayName, actual.displayName)
    }

    @Test(expected = NoLoggedInUserException::class)
    fun `Should throw exception when provider returns null user`() = runTest {
        every { provider.getUser() } returns null

        sourceInTest.getLoggedInUser()

        verify { provider.getUser() }
    }

    @Test
    fun `Should sign in user`() = runTest {
        val callbackSlot = slot<SignInCallback>()
        every { provider.signInUser(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onSuccess()
        }

        sourceInTest.loginUser("valid@email.com", "P@ssw0rd!")

        verify { provider.signInUser("valid@email.com", "P@ssw0rd!", callbackSlot.captured) }
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not sign in user due to exception`() = runTest {
        val expectedException = IllegalStateException()
        val callbackSlot = slot<SignInCallback>()
        every { provider.signInUser(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onError(expectedException)
        }

        sourceInTest.loginUser("valid@email.com", "P@ssw0rd!")

        verify { provider.signInUser("valid@email.com", "P@ssw0rd!", callbackSlot.captured) }
    }

    @Test
    fun `Should sign up user`() = runTest {
        val callbackSlot = slot<SignUpCallback>()
        every { provider.signUpUser(any(), any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onSuccess()
        }

        sourceInTest.signUpUser("Display", "valid@email.com", "P@ssw0rd!")

        verify { provider.signUpUser("Display", "valid@email.com", "P@ssw0rd!", callbackSlot.captured) }
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not sign up user due to exception`() = runTest {
        val expectedException = IllegalStateException()
        val callbackSlot = slot<SignUpCallback>()
        every { provider.signUpUser(any(), any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onError(expectedException)
        }

        sourceInTest.signUpUser("Display", "valid@email.com", "P@ssw0rd!")

        verify { provider.signUpUser("Display", "valid@email.com", "P@ssw0rd!", callbackSlot.captured) }
    }

    @Test
    fun `Should call sign out in provider`() {
        every { provider.signOutUser() } just runs

        sourceInTest.signOutUser()

        verify { provider.signOutUser() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}