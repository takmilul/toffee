package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.CustomerInfoSignIn
import javax.inject.Inject

class ApiLogin @Inject constructor(private val pref: SessionPreference, private val authApi: AuthApi) {

    suspend fun execute(): CustomerInfoSignIn {
        val response = tryIO2 { authApi.apiLogin(getApiLoginRequest()) }
        response.customerInfoSignIn?.let { customerInfoSignIn ->
            pref.saveCustomerInfo(customerInfoSignIn)
        }
        return response.customerInfoSignIn !!
    }

    private fun getApiLoginRequest(): ApiLoginRequest {
        Log.d("FCM_", "getApiLoginRequest: ${pref.fcmToken}")
        return ApiLoginRequest(
            pref.customerId,
            pref.password,
            pref.latitude,
            pref.longitude,
            fcmToken = pref.fcmToken
        )
    }
}