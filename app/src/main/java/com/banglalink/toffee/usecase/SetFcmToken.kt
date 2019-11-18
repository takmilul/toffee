package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.FcmTokenRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class SetFcmToken(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(token:String){
        val response =tryIO {
            toffeeApi.setFcmToken(FcmTokenRequest(token,preference.customerId))
        }
        preference.fcmToken = token
    }
}