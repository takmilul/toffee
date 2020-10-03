package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn

class ApiLogin(private val pref: Preference, private val authApi: AuthApi) {


    suspend fun execute():CustomerInfoSignIn{
        val response = tryIO2 { authApi.apiLogin(getApiLoginRequest()) }
        response.customerInfoSignIn?.let { customerInfoSignIn ->
            pref.saveCustomerInfo(customerInfoSignIn)
        }
        return response.customerInfoSignIn!!
    }
    private fun getApiLoginRequest() =
        ApiLoginRequest(
            pref.customerId,
            pref.password,
            pref.latitude,
            pref.longitude,
            fcmToken = pref.fcmToken
        )

}