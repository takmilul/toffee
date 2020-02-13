package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class SendHeartBeat(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
) {

    suspend fun execute(contentId: Int, contentType: String) {
        val response = tryIO {
            toffeeApi.sendHeartBeat(
                HeartBeatRequest(
                    contentId,
                    contentType,
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude
                )
            )
        }
        preference.sessionToken = response.response.sessionToken ?: ""
    }
}