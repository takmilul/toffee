package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.RedeemReferralCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.RedeemReferralCodeBean
import javax.inject.Inject

class RedeemReferralCode @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(referralCode: String): RedeemReferralCodeBean? {
        val response = tryIO {
            toffeeApi.redeemReferralCode(RedeemReferralCodeRequest(referralCode, preference.customerId, preference.password))
        }
        return response.response
    }
}