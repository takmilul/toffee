package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.apiservice.SignInByPhone
import com.banglalink.toffee.data.network.request.SigninByPhoneRequest
import com.banglalink.toffee.data.network.response.SignInByPhoneResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.SignInByPhoneBean
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito


class SignInByPhoneTest :BaseUseCaseTest(){

    @Test
    fun signInByPhoneSuccess(){

        runBlocking {
            //set up test
            setupPref()
            val signInByPhone = SignInByPhone(SessionPreference.getInstance(), mockToffeeApi)
            Mockito.`when`(mockToffeeApi.signInByPhone(any<SigninByPhoneRequest>())).thenReturn(
                SignInByPhoneResponse(
                    SignInByPhoneBean(true,
                        "LGQRT==")
                ))
            //test method
            signInByPhone.execute("880123456789","")
            //verify it
            verify(mockToffeeApi, times(1)).signInByPhone(any<SigninByPhoneRequest>())
            verify(mockEditor, times(1)).putString("p_number","880123456789")

        }

    }
}