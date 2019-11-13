package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.SigninByPhoneRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class SigninByPhone(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(phoneNumber: String, referralCode: String) {
        val response = tryIO {
            toffeeApi.signinByPhone(
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
    }
}