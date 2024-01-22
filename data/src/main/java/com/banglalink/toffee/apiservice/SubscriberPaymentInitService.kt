package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.response.SubscriberPaymentInitBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class SubscriberPaymentInitService @Inject constructor(
    private val toffeeApi: ToffeeApi
) {
    suspend fun execute(paymentType: String, subscriberPaymentInitRequest: SubscriberPaymentInitRequest): SubscriberPaymentInitBean? {
        val response = tryIO {
            toffeeApi.getSubscriberPaymentInit(
                paymentType = paymentType,
                subscriberPaymentInitRequest = subscriberPaymentInitRequest
            )
        }
        return response.response
    }
}