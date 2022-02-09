package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.FcmTokenRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class SetFcmToken @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(token: String) {
        val savedToken = preference.fcmToken
        if (savedToken != token) {//check is it new token or not
            tryIO2 {
                toffeeApi.setFcmToken(FcmTokenRequest(token, preference.customerId))
            }
            preference.fcmToken = token
        }
    }
}