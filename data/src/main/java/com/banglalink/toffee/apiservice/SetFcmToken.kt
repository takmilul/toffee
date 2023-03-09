package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.FcmTokenRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class SetFcmToken @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(token: String) {
        val savedToken = preference.fcmToken
        if (savedToken != token && preference.customerId != 0 && preference.password.isNotBlank()) {//check is it new token or not
            tryIO {
                toffeeApi.setFcmToken(FcmTokenRequest(token, preference.customerId))
            }
            preference.fcmToken = token
        }
    }
}