package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.SigninByPhoneRequest
import com.banglalink.toffee.data.network.response.SigninByPhoneResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.SigninByPhoneBean
import com.banglalink.toffee.usecase.SigninByPhone
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response


class SigninByPhoneTest :BaseUseCaseTest(){

    @Mock
    val mockToffeeApi: ToffeeApi = mock()

    @Test
    fun signin_by_phone_success(){

        runBlocking {
            //set up test
            setupPref()
            val signinByPhone = SigninByPhone(Preference.getInstance(), mockToffeeApi)
            Mockito.`when`(mockToffeeApi.signinByPhone(any<SigninByPhoneRequest>())).thenReturn(
                Response.success(SigninByPhoneResponse(
                    SigninByPhoneBean(true)
                )))
            //test method
            signinByPhone.execute("880123456789","")
            //verify it
            verify(mockToffeeApi, times(1)).signinByPhone(any<SigninByPhoneRequest>())
            verify(mockEditor, times(1)).putString("p_number","880123456789")

        }

    }
}