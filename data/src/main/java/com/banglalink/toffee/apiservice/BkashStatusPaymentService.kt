package com.banglalink.toffee.apiservice


import com.banglalink.toffee.data.network.request.QueryPaymentRequest
import com.banglalink.toffee.data.network.response.QueryPaymentResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class BkashStatusPaymentService @Inject constructor(private val mPref: SessionPreference, private val api: ExternalApi) {
    
    suspend fun execute(token: String, requestBody: QueryPaymentRequest): QueryPaymentResponse {
        return tryIO2 {
            api.statusPayment(
                mPref.bkashQueryPaymentUrl,
                "Bearer $token",
                mPref.bkashAppKey,
                requestBody
            )
        }
    }
}