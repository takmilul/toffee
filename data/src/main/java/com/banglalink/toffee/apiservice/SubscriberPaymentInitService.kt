package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PackVoucherMethodRequest
import com.banglalink.toffee.data.network.request.PremiumPackDetailRequest
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.response.SubscriberPaymentInitBean
import com.banglalink.toffee.data.network.response.VoucherPaymentMethodResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.VoucherPaymentBean
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