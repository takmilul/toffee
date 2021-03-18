package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.SigninByPhoneRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class SignInByPhone @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(phoneNumber: String, referralCode: String): String {
        val response = tryIO2 {
            toffeeApi.signInByPhone(
                SigninByPhoneRequest(
                    phoneNumber,
                    preference.latitude,
                    preference.longitude,
                    referralCode = referralCode
                )
            )

        }
        if (response.response.authorize) {
            preference.phoneNumber = phoneNumber
        }
        return response.response.regSessionToken;
    }
}