package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HeartBeatRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class SendHeartBeat(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
) {

    suspend fun execute(contentId: Int, contentType: String, isNetworkSwitch: Boolean = false) {
        var needToRefreshSessionToken = isNetworkSwitch
        if(System.currentTimeMillis() - preference.getSessionTokenSaveTimeInMillis() > preference.getSessionTokenLifeSpanInMillis()){
            needToRefreshSessionToken = true// we need to refresh token by setting isNetworkSwitch = true
        }
        val response = tryIO {
            toffeeApi.sendHeartBeat(
                HeartBeatRequest(
                    contentId,
                    contentType,
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude,
                    isNetworkSwitch = needToRefreshSessionToken
                )
            )
        }
        preference.sessionToken = response.response.sessionToken ?: ""
        preference.setHeaderSessionToken(response.response.headerSessionToken)

    }
}