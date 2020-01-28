package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.RedeemReferralCodeRequest
import com.banglalink.toffee.data.network.response.RedeemReferralCodeResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class RedeemReferralCode(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(referralCode: String) : RedeemReferralCodeResponse{
        val response = tryIO{
            toffeeApi.redeemReferralCode( RedeemReferralCodeRequest(referralCode, preference.customerId, preference.password))
        }

        return response;
    }

}