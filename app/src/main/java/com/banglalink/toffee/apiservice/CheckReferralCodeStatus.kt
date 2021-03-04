package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ReferralCodeStatusRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.exception.ReferralException
import com.banglalink.toffee.model.INVALID_REFERRAL_ERROR_CODE
import javax.inject.Inject

class CheckReferralCodeStatus @Inject constructor(val toffeeApi: ToffeeApi) {

    suspend fun execute(phoneNumber: String, referralCode: String) {
        val response = tryIO2 { toffeeApi.checkReferralCode(ReferralCodeStatusRequest(phoneNumber, referralCode)) }
        if (! response.response.referralStatus.equals("VALID", ignoreCase = true)) {
            throw ReferralException(INVALID_REFERRAL_ERROR_CODE, response.response.referralStatusMessage, response.response.referralStatus)
        }
    }
}