package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PremiumPackDetailRequest
import com.banglalink.toffee.data.network.response.PremiumPackDetailBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class PremiumPackDetailService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun loadData(packId: Int): PremiumPackDetailBean? {
        val response = tryIO2 {
            toffeeApi.getPremiumPackDetail(
                packId,
                preference.getDBVersionByApiName(ApiNames.GET_PREMIUM_PACK_DETAIL),
                PremiumPackDetailRequest(
                    preference.customerId,
                    preference.password,
                )
            )
        }
        return response.response
    }
}