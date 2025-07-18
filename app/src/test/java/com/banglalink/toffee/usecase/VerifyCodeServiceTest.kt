package com.banglalink.toffee.usecase

import com.banglalink.toffee.apiservice.VerifyCodeService
import com.banglalink.toffee.data.exception.ApiException
import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.response.VerifyCodeResponse
import com.banglalink.toffee.data.repository.BubbleConfigRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.CustomerInfoLogin
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Response

class VerifyCodeServiceTest :BaseUseCaseTest(){
    
    @Mock var bubbleConfigRepository: BubbleConfigRepository = mock()
    
    @Test
    fun verify_code_success(){

        runBlocking {
            //set up test
            setupPref()
            val verifyCodeService = VerifyCodeService(mockToffeeApi, SessionPreference.getInstance(), bubbleConfigRepository)
            Mockito.`when`(mockToffeeApi.verifyCode(any<VerifyCodeRequest>())).thenReturn(
                Response.success(VerifyCodeResponse(
                    CustomerInfoLogin().apply {
//                        customerId = 1729
//                        customerName = null

                    }
                )).body())
            //test method
            verifyCodeService.execute("12345","LGTY==")
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
                val verifyCodeService = VerifyCodeService(mockToffeeApi, SessionPreference.getInstance(), bubbleConfigRepository)
                Mockito.`when`(mockToffeeApi.verifyCode(any<VerifyCodeRequest>())).thenReturn(
                    Response.success(VerifyCodeResponse(
                        CustomerInfoLogin()
                    ).apply {
                        errorCode = 1
                        errorMsg = "Invalid code"
                    }
                    ).body())
                //test method
                verifyCodeService.execute("12345","LGTY==")
                //verify it
                verify(mockToffeeApi).verifyCode(check {
                    assertEquals(it.code,"12345")
                })
                verify(mockToffeeApi, times(1)).verifyCode(any<VerifyCodeRequest>())

            }catch (e: ApiException){
                assertEquals("Invalid code",e.errorMessage)
                assertEquals(1,e.errorCode)
                throw e
            }

        }

    }
}