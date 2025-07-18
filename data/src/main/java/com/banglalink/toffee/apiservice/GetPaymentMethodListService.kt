package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PaymentMethodRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Payment
import javax.inject.Inject

class GetPaymentMethodListService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
): BaseApiService<Payment> {
    override suspend fun loadData(offset: Int, limit: Int): List<Payment> {
        
        val response = tryIO {
            toffeeApi.getPaymentMethodList(
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_PAYMENT_METHOD_LIST),
                PaymentMethodRequest(preference.customerId,preference.password)
            )
        }
        return response.response?.paymentList ?: emptyList()
    }
}