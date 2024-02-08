package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.LoginByPhoneRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class LoginByPhone @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(phoneNumber: String, referralCode: String): String {
        val response = tryIO {
            toffeeApi.loginByPhone(
                LoginByPhoneRequest(
                    phoneNumber,
                    preference.latitude,
                    preference.longitude,
                    referralCode = referralCode
                )
            )

        }
        /*if (response.response.authorize) {
            preference.phoneNumber = phoneNumber
        }*/
        preference.newUser.value = response.response.userType
        return response.response.regSessionToken
    }
}