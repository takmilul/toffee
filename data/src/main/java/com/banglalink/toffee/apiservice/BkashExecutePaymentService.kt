package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.response.ExecutePaymentResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class BkashExecutePaymentService @Inject constructor(private val mPref: SessionPreference, private val api: ExternalApi) {
    
    suspend fun execute(token: String, requestBody: ExecutePaymentRequest): ExecutePaymentResponse {
        return tryIOExternal {
            api.executePayment(
                mPref.bkashExecuteUrl,
                "Bearer $token",
                mPref.bkashAppKey,
                requestBody
            )
        }
    }
}