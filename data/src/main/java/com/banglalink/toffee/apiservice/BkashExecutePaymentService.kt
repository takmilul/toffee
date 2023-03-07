package com.banglalink.toffee.apiservice

import com.banglalink.toffee.Constants.BKASH_APP_KEY
import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.response.CreatePaymentResponse
import com.banglalink.toffee.data.network.response.ExecutePaymentResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class BkashExecutePaymentService @Inject constructor(private val mPref: SessionPreference, private val api: ExternalApi) {
    
    suspend fun execute(token: String, requestBody: ExecutePaymentRequest): ExecutePaymentResponse {
        return tryIO2 {
            api.executePayment(
                "https://tokenized.sandbox.bka.sh/v1.2.0-beta/tokenized/checkout/execute",
                "Bearer $token",
                BKASH_APP_KEY,
                requestBody
            )
        }
    }
}