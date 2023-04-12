package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.repository.BubbleConfigRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.CustomerInfoLogin
import javax.inject.Inject

class ApiLoginService @Inject constructor(
    private val authApi: AuthApi,
    private val pref: SessionPreference,
    private val bubbleConfigRepository: BubbleConfigRepository
) {
    
    suspend fun execute(): CustomerInfoLogin {
        val response = tryIO { authApi.apiLogin(getApiLoginRequest()) }
        response.customerInfoLogin?.let {
            try {
                it.bubbleConfig?.let { bubbleConfigRepository.insert(it) }
            } catch (e: Exception) {
                Log.i("bubble_", "execute: ${e.message}")
            }
            pref.saveCustomerInfo(it)
        }
        return response.customerInfoLogin!!
    }
    
    private fun getApiLoginRequest(): ApiLoginRequest {
        Log.d("FCM_", "getApiLoginRequest: ${pref.fcmToken}")
        return ApiLoginRequest(
            pref.customerId,
            pref.password,
            pref.phoneNumber,
            pref.latitude,
            pref.longitude,
            fcmToken = pref.fcmToken
        )
    }
}