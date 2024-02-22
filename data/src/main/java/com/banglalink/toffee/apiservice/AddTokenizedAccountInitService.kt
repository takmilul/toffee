package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.AddTokenizedAccountInitRequest
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.response.SubscriberPaymentInitBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class AddTokenizedAccountInitService @Inject constructor(
    private val toffeeApi: ToffeeApi
) {
    suspend fun execute(paymentType: String, addTokenizedAccountInitRequest: AddTokenizedAccountInitRequest): SubscriberPaymentInitBean? {
        val response = tryIO {
            toffeeApi.getAddTokenizedAccountInit(
                paymentType = paymentType,
                addTokenizedAccountInitRequest = addTokenizedAccountInitRequest
            )
        }
        return response.response
    }
}