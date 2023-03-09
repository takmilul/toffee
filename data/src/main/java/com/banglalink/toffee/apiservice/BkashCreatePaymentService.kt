package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.response.CreatePaymentResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class BkashCreatePaymentService @Inject constructor(private val mPref: SessionPreference, private val api: ExternalApi) {
    
    suspend fun execute(token: String, requestBody: CreatePaymentRequest): CreatePaymentResponse {
        return tryIOExternal {
            api.createPayment(
                mPref.bkashCreateUrl,
                "Bearer $token",
                mPref.bkashAppKey,
                requestBody
            )
        }
    }
}