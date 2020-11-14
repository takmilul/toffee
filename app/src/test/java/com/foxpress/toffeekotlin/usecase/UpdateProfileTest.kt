package com.foxpress.toffeekotlin.usecase

import com.banglalink.toffee.data.network.request.UpdateProfileRequest
import com.banglalink.toffee.data.network.response.UpdateProfileResponse
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.apiservice.UpdateProfile
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response


class UpdateProfileTest :BaseUseCaseTest(){


    @Test
    fun update_profile_success(){

        runBlocking {
            //set up test
            setupPref()
            val apiLogin = UpdateProfile(Preference.getInstance(), mockToffeeApi)
            Mockito.`when`(mockToffeeApi.updateProfile(any<UpdateProfileRequest>())).thenReturn(
                Response.success(UpdateProfileResponse(
                )))
            //test method
            apiLogin.execute("name","e@e.com","my address","12345")
            //verify it
            verify(mockToffeeApi, times(1)).updateProfile(any<UpdateProfileRequest>())
            verify(mockToffeeApi).updateProfile(check {
                assertEquals(it.fullname,"name")
                assertEquals(it.email,"e@e.com")
                assertEquals(it.address,"my address")
                assertEquals(it.phoneNo,"12345")
            })
            verify(mockEditor).putString("customer_name","name")

        }

    }

    @Test
    fun api_login_no_account_found(){

        expectedException.expect(ApiException::class.java)
        expectedException.expectMessage("")//for 109 error msg should be empty
        runBlocking {
            //set up test
            setupPref()
            val apiLogin = UpdateProfile(Preference.getInstance(), mockToffeeApi)
            Mockito.`when`(mockToffeeApi.updateProfile(any<UpdateProfileRequest>())).thenReturn(
                Response.success(UpdateProfileResponse().apply {
                    errorCode = 109
                    errorMsg = "No account found"
                }))
            //test method
            apiLogin.execute("name","e@e.com","my address","12345")
        }

    }
}