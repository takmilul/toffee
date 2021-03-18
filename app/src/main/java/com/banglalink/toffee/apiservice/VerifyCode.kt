package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.CustomerInfoSignIn
import javax.inject.Inject

class VerifyCode @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {

    suspend fun execute(code: String, regSessionToken: String, referralCode: String = ""): CustomerInfoSignIn {
        val response = tryIO2 { toffeeApi.verifyCode(getRequest(code, regSessionToken, referralCode)) }

        response.customerInfoSignIn.also { customerInfoSignIn ->
            preference.saveCustomerInfo(customerInfoSignIn)
        }

        return response.customerInfoSignIn
    }

    private fun getRequest(code: String, regSessionToken: String, referralCode: String): VerifyCodeRequest {
        return VerifyCodeRequest(code, regSessionToken, referralCode, preference.fcmToken, preference.latitude, preference.longitude)
    }
}