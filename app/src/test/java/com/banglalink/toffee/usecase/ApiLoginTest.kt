package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.response.ApiLoginResponse
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.apiservice.ApiLogin
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response


class ApiLoginTest :BaseUseCaseTest(){

    @Mock
    var mockAuthApi:AuthApi = mock()

    @Test
    fun api_login_success(){

        runBlocking {
            //set up test
            setupPref()
            val apiLogin = ApiLogin(SessionPreference.getInstance(), mockAuthApi)
            Mockito.`when`(mockAuthApi.apiLogin(any<ApiLoginRequest>())).thenReturn(
//                Response.success(ApiLoginResponse(
//                    CustomerInfoLogin().apply {
//                        balance = 0
//                        authorize = true
//                        customerId = 1729
//                        password = "r212R"
//                        sessionToken = "U=9322ETGEW#$"
//
//                    }
//                ))
                    null
                )
            //test method
            apiLogin.execute()
            //verify it
            verify(mockAuthApi).apiLogin(check {
                assertEquals(it.apiName,"apiLogin")
            })
            verify(mockAuthApi, times(1)).apiLogin(any<ApiLoginRequest>())
            verify(mockEditor).putInt("balance",0)
            verify(mockEditor).putInt("customer_id",1729)
            verify(mockEditor).putString("sessionToken","U=9322ETGEW#$")
            //password should not be stored...thats why invocation is set to 0
            verify(mockEditor, times(0)).putString("passwd","r212R")

        }

    }

    @Test
    fun api_login_no_account_found(){

        expectedException.expect(ApiException::class.java)
        expectedException.expectMessage("")//for 109 error msg should be empty
        runBlocking {
            //set up test
            setupPref()
            val apiLogin = ApiLogin(SessionPreference.getInstance(), mockAuthApi)
            Mockito.`when`(mockAuthApi.apiLogin(any<ApiLoginRequest>())).thenReturn(
//                Response.success(ApiLoginResponse(
//                   null
//                ).apply {
//                    errorCode = 109
//                    errorMsg = "No account found"
//                })
                null
            )
            //test method
            apiLogin.execute()
        }

    }
}