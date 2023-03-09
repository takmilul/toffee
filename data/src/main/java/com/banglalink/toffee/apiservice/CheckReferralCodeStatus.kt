package com.banglalink.toffee.apiservice

import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.exception.ReferralException
import com.banglalink.toffee.data.network.request.ReferralCodeStatusRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class CheckReferralCodeStatus @Inject constructor(val toffeeApi: ToffeeApi) {

    suspend fun execute(phoneNumber: String, referralCode: String) {
        val response = tryIO { toffeeApi.checkReferralCode(ReferralCodeStatusRequest(phoneNumber, referralCode)) }
        if (! response.response.referralStatus.equals("VALID", ignoreCase = true)) {
            throw ReferralException(Constants.INVALID_REFERRAL_ERROR_CODE, response.response.referralStatusMessage, response.response.referralStatus)
        }
    }
}