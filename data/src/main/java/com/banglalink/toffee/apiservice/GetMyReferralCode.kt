package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ReferralCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ReferralCodeBean
import javax.inject.Inject

class GetMyReferralCode @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute():ReferralCodeBean{
        val response = tryIO2{
            toffeeApi.getMyReferralCode(ReferralCodeRequest(preference.customerId,preference.password))
        }
        return response.response
    }
}