package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.TokenizedPaymentMethodsApiRequest
import com.banglalink.toffee.data.network.response.TokenizedPaymentMethodsApiResponse
import com.banglalink.toffee.data.network.response.TokenizedPaymentMethodsBaseApiResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class TokenizedPaymentMethodApiService @Inject constructor(
    private val toffeeApi: ToffeeApi
) {
    suspend fun execute(body: TokenizedPaymentMethodsApiRequest): TokenizedPaymentMethodsApiResponse? {
        val mainResponse = tryIO { toffeeApi.getTokenizedPaymentMethods(body) }
        return mainResponse.response
    }
}