package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.response.VerifyCodeResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.apiservice.VerifyCode
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response


class VerifyCodeTest :BaseUseCaseTest(){

    @Test
    fun verify_code_success(){

        runBlocking {
            //set up test
            setupPref()
            val verifyCode = VerifyCode(SessionPreference.getInstance(), mockToffeeApi)
            Mockito.`when`(mockToffeeApi.verifyCode(any<VerifyCodeRequest>())).thenReturn(
                Response.success(VerifyCodeResponse(
                    CustomerInfoLogin().apply {
                        customerId = 1729
                        customerName = null

                    }
                )))
            //test method
            verifyCode.execute("12345","LGTY==")
            //verify it
            verify(mockToffeeApi).verifyCode(check {
                assertEquals(it.code,"12345")
            })
            verify(mockToffeeApi, times(1)).verifyCode(any<VerifyCodeRequest>())
            verify(mockEditor, times(1)).putInt("customer_id",1729)
            verify(mockEditor, times(1)).putString("customer_name","")

        }

    }

    @Test(expected = ApiException::class)
    fun verify_code_failure_invalid_code(){

        runBlocking {
            try{
                //set up test
                setupPref()
                val verifyCode = VerifyCode(SessionPreference.getInstance(), mockToffeeApi)
                Mockito.`when`(mockToffeeApi.verifyCode(any<VerifyCodeRequest>())).thenReturn(
                    Response.success(VerifyCodeResponse(
                        CustomerInfoLogin()
                    ).apply {
                        errorCode = 1
                        errorMsg = "Invalid code"
                    }
                    ))
                //test method
                verifyCode.execute("12345","LGTY==")
                //verify it
                verify(mockToffeeApi).verifyCode(check {
                    assertEquals(it.code,"12345")
                })
                verify(mockToffeeApi, times(1)).verifyCode(any<VerifyCodeRequest>())

            }catch (e:ApiException){
                assertEquals("Invalid code",e.errorMessage)
                assertEquals(1,e.errorCode)
                throw e
            }

        }

    }
}