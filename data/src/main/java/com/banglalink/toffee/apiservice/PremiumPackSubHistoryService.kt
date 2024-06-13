package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PremiumPackSubHistoryRequest
import com.banglalink.toffee.data.network.response.SubHistoryResponseBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class PremiumPackSubHistoryService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend fun execute(): SubHistoryResponseBean? {
        val mainResponse = tryIO {
            toffeeApi.getPremiumPackSubscriptionHistory(
                PremiumPackSubHistoryRequest(
                    customerId = preference.customerId,
                    password = preference.password
                )
            )
        }
        return mainResponse.response
    }
}