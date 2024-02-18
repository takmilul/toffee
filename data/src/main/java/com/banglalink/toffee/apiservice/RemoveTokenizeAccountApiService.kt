package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.RemoveTokenizedAccountApiRequest
import com.banglalink.toffee.data.network.response.RemoveTokenizeAccountApiResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class RemoveTokenizeAccountApiService @Inject constructor(
    private val toffeeApi: ToffeeApi
) {
    suspend fun execute(paymentMethodId: Int, body: RemoveTokenizedAccountApiRequest) : RemoveTokenizeAccountApiResponse?{
        val mainResponse = tryIO { toffeeApi.removeTokenizeAccount(paymentMethodId, body) }
        return mainResponse.response
    }
}