package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ReferralCodeStatusRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.exception.ReferralException
import com.banglalink.toffee.model.INVALID_REFERRAL_ERROR_CODE

class CheckReferralCodeStatus(val toffeeApi: ToffeeApi) {

    suspend fun execute(phoneNumber:String,referralCode:String){
        val response = tryIO { toffeeApi.checkReferralCode(ReferralCodeStatusRequest(phoneNumber,referralCode)) }
        if(response.response.referralStatus != "VALID"){
            throw ReferralException(INVALID_REFERRAL_ERROR_CODE,response.response.referralStatusMessage, response.response.referralStatus)
        }
    }
}