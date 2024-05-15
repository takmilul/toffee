package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.DobValidateOtpRequest
import com.banglalink.toffee.data.network.response.DobValidateOtpResponseBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class DobValidateOtpApiService @Inject constructor(
    private val toffeeApi: ToffeeApi
) {
    suspend fun execute(body: DobValidateOtpRequest): DobValidateOtpResponseBean? {
        val baseResponse = tryIO { toffeeApi.dobValidateOtp(body) }
        return baseResponse.response
    }
}