package com.rpc.weatherapp.providers

import android.app.Activity
import android.text.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.rpc.weatherapp.core.providers.AuthenticationProvider
import com.rpc.weatherapp.core.providers.AuthenticationProviderImpl
import com.rpc.weatherapp.core.providers.InvalidCredentialsException
import com.rpc.weatherapp.core.providers.SignInCallback
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.Executor
import kotlin.math.exp

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

        verify { callback.onSuccess() }
    }

    @Test
    fun `Should trigger onSuccess callback when sign in is not successful`() {
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


    private fun generateSuccessTask(expected: AuthResult): Task<AuthResult> {
        return object: Task<AuthResult>() {
            override fun isComplete() = true
            override fun isSuccessful() = true

            override fun addOnFailureListener(p0: OnFailureListener): Task<AuthResult> {
                return this
            }

            override fun addOnFailureListener(
                p0: Activity,
                p1: OnFailureListener
            ): Task<AuthResult> {
                return this
            }

            override fun addOnFailureListener(
                p0: Executor,
                p1: OnFailureListener
            ): Task<AuthResult> {
                return this
            }

            override fun getException(): Exception? = null

            override fun getResult(): AuthResult = expected

            override fun <X : Throwable?> getResult(p0: Class<X>): AuthResult = expected

            override fun addOnSuccessListener(
                p0: Executor,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> = this

            override fun addOnSuccessListener(
                p0: Activity,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> = this

            override fun addOnSuccessListener(p0: OnSuccessListener<in AuthResult>) = this

            override fun isCanceled() = false
            override fun addOnCompleteListener(callbcks: OnCompleteListener<AuthResult>): Task<AuthResult> {
                callbcks.onComplete(this)
                return this
            }

        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}