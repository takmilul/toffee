package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.TokenizedAccountInfoApiRequest
import com.banglalink.toffee.data.network.response.TokenizedAccountInfo
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class TokenizedAccountInfoApiService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val mPref: SessionPreference
) {
    suspend fun execute(paymentMethodId: Int, body: TokenizedAccountInfoApiRequest): List<TokenizedAccountInfo>? {
        val mainResponse = tryIO { toffeeApi.getTokenizedAccountInfo(paymentMethodId, body) }
        mPref.tokenizedAccountInfoList.value = mainResponse.response
        return mainResponse.response
    }
}