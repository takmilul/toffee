package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ReferralCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class GetMyReferralCode(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute():String{
        val response = tryIO{
            toffeeApi.getMyReferralCode(ReferralCodeRequest(preference.customerId,preference.password))
        }
        return response.response.referralCode
    }
}