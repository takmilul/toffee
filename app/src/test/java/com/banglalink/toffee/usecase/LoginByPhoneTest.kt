package com.banglalink.toffee.usecase

import com.banglalink.toffee.apiservice.LoginByPhone
import com.banglalink.toffee.data.network.request.LoginByPhoneRequest
import com.banglalink.toffee.data.network.response.LoginByPhoneResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.LoginByPhoneBean
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito


class LoginByPhoneTest :BaseUseCaseTest(){

    @Test
    fun loginByPhoneSuccess(){

        runBlocking {
            //set up test
            setupPref()
            val loginByPhone = LoginByPhone(SessionPreference.getInstance(), mockToffeeApi)
            Mockito.`when`(mockToffeeApi.loginByPhone(any<LoginByPhoneRequest>())).thenReturn(
                LoginByPhoneResponse(
                    LoginByPhoneBean(true,
                        "LGQRT==")
                ))
            //test method
            loginByPhone.execute("880123456789","")
            //verify it
            verify(mockToffeeApi, times(1)).loginByPhone(any<LoginByPhoneRequest>())
            verify(mockEditor, times(1)).putString("p_number","880123456789")

        }

    }
}