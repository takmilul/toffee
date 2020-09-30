package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.RedeemReferralCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.RedeemReferralCodeBean

class RedeemReferralCode(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(referralCode: String) : RedeemReferralCodeBean{
        val response = tryIO2{
            toffeeApi.redeemReferralCode( RedeemReferralCodeRequest(referralCode, preference.customerId, preference.password))
        }
        return response.response
    }

}