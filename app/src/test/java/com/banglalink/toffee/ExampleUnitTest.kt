package com.banglalink.toffee

import com.banglalink.toffee.apiservice.CheckForUpdateService
import com.banglalink.toffee.data.exception.UpdateRequiredException
import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.response.CheckUpdateResponse
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.CheckUpdateBean
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mock
import retrofit2.Response


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](https://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Rule
    @JvmField
    var expectedException = ExpectedException.none()

    @Mock
    val mock:AuthApi = mock()

    @Mock
    val pref: SessionPreference = mock()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun throw_force_update_required_exception(){

        expectedException.expect(UpdateRequiredException::class.java)
        expectedException.expectMessage("msg")
        /* Given */
        val mock = mock<AuthApi> {
            on {
                runBlocking {
                    checkForUpdate(CheckUpdateRequest("1"))
                }
            } doReturn Response.success(CheckUpdateResponse(CheckUpdateBean(2, "msg", "title"))).body()!!
        }
        val classUnderTest = CheckForUpdateService(pref, mock)

        /* When */
        runBlocking {
            given(classUnderTest.execute("1")).willThrow(
                UpdateRequiredException(
                    "title",
                    "msg",
                    true
                )
            )
        }
    }

    @Test
    fun throw_optional_update_required_exception(){

        expectedException.expect(UpdateRequiredException::class.java)
        expectedException.expectMessage("msg")
        /* Given */
        val mock = mock<AuthApi> {
            on {
                runBlocking {
                    checkForUpdate(CheckUpdateRequest("1"))
                }
            } doReturn Response.success(CheckUpdateResponse(CheckUpdateBean(1, "msg", "title"))).body()!!
        }
        val classUnderTest = CheckForUpdateService(pref, mock)

        /* When */
        runBlocking {
            given(classUnderTest.execute("1")).willThrow(
                UpdateRequiredException(
                    "title",
                    "msg",
                    false
                )
            )
        }
    }

    @Test
    fun no_update_found(){

        runBlocking {

            val classUnderTest = CheckForUpdateService(pref, mock)
            whenever(mock.checkForUpdate(any<CheckUpdateRequest>())).thenReturn(Response.success(
                CheckUpdateResponse(CheckUpdateBean(0, "msg", "title"))
            ).body())

            classUnderTest.execute("1")
            verify(mock).checkForUpdate(check {
                assertEquals("1", it.versionCode)
            })
        }


    }
}
