package com.rpc.weatherapp.providers

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.rpc.weatherapp.core.domain.User
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import com.rpc.weatherapp.core.providers.AuthenticationProviderImpl
import com.rpc.weatherapp.core.providers.InvalidCredentialsException
import com.rpc.weatherapp.core.providers.SignInCallback
import com.rpc.weatherapp.core.providers.SignUpCallback
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
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthenticationProviderTests {

    @MockK
    private lateinit var auth: FirebaseAuth

    private lateinit var providerInTest: AuthenticationProvider
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        providerInTest = AuthenticationProviderImpl(auth)
    }

    @Test
    fun `Should return not return current user if Firebase's currentUser is null`() {
        every { auth.currentUser } returns null

        val actual = providerInTest.getUser()

        assertNull(actual)
    }

    @Test
    fun `Should return not return current user if Firebase's currentUser is not null`() {
        val currentUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns currentUser

        val actual = providerInTest.getUser()

        assertNotNull(actual)
    }

    @Test
    fun `Should trigger onSuccess callback when sign in is successful`() {
        val expected = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { isCanceled } returns false
        }
        val successSlot = slot<OnCompleteListener<AuthResult>>()
        every { auth.signInWithEmailAndPassword(any(), any()).addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignInCallback> {
            every { onSuccess() } just runs
        }
        providerInTest.signInUser("valid@email.com", "P@ssw0rd!", callback)

        verify { auth.signInWithEmailAndPassword("valid@email.com", "P@ssw0rd!") }
        verify { callback.onSuccess() }
    }

    @Test
    fun `Should trigger onError callback with InvalidCredentialsException when sign in is not successful`() {
        val expectedException = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_INVALID_CREDENTIAL"
        }
        val expected = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns false
            every { exception } returns expectedException
        }
        val successSlot = slot<OnCompleteListener<AuthResult>>()
        every { auth.signInWithEmailAndPassword(any(), any()).addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignInCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.signInUser("valid@email.com", "P@ssw0rd!", callback)

        verify { callback.onError(withArg { it is InvalidCredentialsException }) }
    }

    @Test
    fun `Should trigger onError callback with IllegalStateException when firebase returns a different ERROR CODE`() {
        val expectedException = mockk<FirebaseAuthException> {
            every { errorCode } returns "ERROR_DIFFERENT"
        }
        val expected = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns false
            every { exception } returns expectedException
        }
        val successSlot = slot<OnCompleteListener<AuthResult>>()
        every { auth.signInWithEmailAndPassword(any(), any()).addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignInCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.signInUser("valid@email.com", "P@ssw0rd!", callback)

        verify { callback.onError(withArg { it !is InvalidCredentialsException && it.message == "Sign in failed due to error code: ERROR_DIFFERENT." }) }
    }

    @Test
    fun `Should trigger onError callback with IllegalStateException when result returns null exception`() {
        val expectedException = IllegalStateException("Failed to sign in due to unknown error.")
        val expected = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns false
            every { exception } returns null
        }
        val successSlot = slot<OnCompleteListener<AuthResult>>()
        every { auth.signInWithEmailAndPassword(any(), any()).addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignInCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.signInUser("valid@email.com", "P@ssw0rd!", callback)

        verify { callback.onError(
            withArg {
                it !is InvalidCredentialsException &&
                it.message == expectedException.message
            })
        }
    }

    @Test
    fun `Should trigger onError callback with Any exception when result returns null exception`() {
        val expectedException = IllegalStateException("Failed to sign in due to unknown error.")
        val expected = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns false
            every { exception } returns expectedException
        }

        val successSlot = slot<OnCompleteListener<AuthResult>>()
        every { auth.signInWithEmailAndPassword(any(), any()).addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignInCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.signInUser("valid@email.com", "P@ssw0rd!", callback)

        verify { callback.onError(
            withArg {
                it !is InvalidCredentialsException &&
                        it.message == expectedException.message
            })
        }
    }

    @Test
    fun `Should trigger onSuccess when sign up is successful`() {
        mockkConstructor(UserProfileChangeRequest.Builder::class)
        val request = mockk<UserProfileChangeRequest>()
        every { anyConstructed<UserProfileChangeRequest.Builder>().setDisplayName(any()) } answers { this.self as UserProfileChangeRequest.Builder }
        every { anyConstructed<UserProfileChangeRequest.Builder>().build() } returns request


        val currentUser = mockk<FirebaseUser>()
        val initialTask = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { isCanceled } returns false
            every { exception } returns null
        }
        val expected = mockk<Task<Void>> {
            every { isComplete } returns true
            every { isSuccessful } returns true
            every { isCanceled } returns false
            every { exception } returns null
        }
        val createUserSlot = slot<Continuation<AuthResult, Task<Void>>>()
        val successSlot = slot<OnCompleteListener<Void>>()
        every { auth.createUserWithEmailAndPassword(any(), any())  } returns initialTask
        every { auth.currentUser } returns currentUser
        every { initialTask.continueWithTask(capture(createUserSlot)) } answers {
            createUserSlot.captured.then(initialTask)
            expected
        }
        every { currentUser.updateProfile(any()) } returns expected
        every { expected.addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignUpCallback> {
            every { onSuccess() } just runs
        }
        providerInTest.signUpUser("Display Name", "valid@email.com", "P@ssw0rd!", callback)

        verify { auth.createUserWithEmailAndPassword("valid@email.com", "P@ssw0rd!") }
        verify { currentUser.updateProfile(request) }
        verify { callback.onSuccess() }
    }

    @Test
    fun `Should trigger onError when sign up is not successful due to unknown error`() {
        val expectedException = IllegalStateException("Failed to sign in due to unknown error.")

        val currentUser = mockk<FirebaseUser>()
        val initialTask = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { isSuccessful } returns false
            every { isCanceled } returns true
            every { exception } returns null
        }
        val expected = mockk<Task<Void>> {
            every { isComplete } returns true
            every { isSuccessful } returns false
            every { isCanceled } returns true
            every { exception } returns null
        }
        val createUserSlot = slot<Continuation<AuthResult, Task<Void>>>()
        val successSlot = slot<OnCompleteListener<Void>>()
        every { auth.createUserWithEmailAndPassword(any(), any())  } returns initialTask
        every { auth.currentUser } returns currentUser
        every { initialTask.continueWithTask(capture(createUserSlot)) } answers {
            createUserSlot.captured.then(initialTask)
            expected
        }
        every { currentUser.updateProfile(any()) } returns expected
        every { expected.addOnCompleteListener(capture(successSlot)) } answers {
            successSlot.captured.onComplete(expected)
            expected
        }

        val callback = mockk<SignUpCallback> {
            every { onError(any()) } just runs
        }
        providerInTest.signUpUser("Display Name", "valid@email.com", "P@ssw0rd!", callback)

        verify { auth.createUserWithEmailAndPassword("valid@email.com", "P@ssw0rd!") }
        verify { callback.onError(withArg { assertTrue(it.message == expectedException.message) }) }
    }

    @Test
    fun `Should call firebase Auth - signOut()`() {
        every { auth.signOut() } just runs
        providerInTest.signOutUser()
        verify { auth.signOut() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}