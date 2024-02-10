package com.rpc.weatherapp.sources

import com.rpc.weatherapp.core.sources.UserDataSource
import com.rpc.weatherapp.core.sources.UserDataSourceImpl
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
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

    @After
    fun tearDown() {
        unmockkAll()
    }
}