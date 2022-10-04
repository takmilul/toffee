package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.CustomerInfoLogin
import javax.inject.Inject

class VerifyCodeService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {

    suspend fun execute(code: String, regSessionToken: String, referralCode: String = ""): CustomerInfoLogin {
        val response = tryIO2 { toffeeApi.verifyCode(getRequest(code, regSessionToken, referralCode)) }

        response.customerInfoLogin.also { customerInfoSignIn ->
            preference.saveCustomerInfo(customerInfoSignIn)
        }

        return response.customerInfoLogin
    }

    private fun getRequest(code: String, regSessionToken: String, referralCode: String): VerifyCodeRequest {
        return VerifyCodeRequest(code, regSessionToken, referralCode, preference.fcmToken, preference.latitude, preference.longitude)
    }
}