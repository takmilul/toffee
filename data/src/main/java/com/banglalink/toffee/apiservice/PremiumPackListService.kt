package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PremiumPackListRequest
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class PremiumPackListService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun loadData(contentId: String): List<PremiumPack> {
        val response = tryIO2 {
            toffeeApi.getPremiumPackList(
                contentId,
                preference.getDBVersionByApiName(ApiNames.GET_PREMIUM_PACK_LIST),
                PremiumPackListRequest(
                    preference.customerId,
                    preference.password,
                )
            )
        }
        return response.response?.premiumPacks ?: emptyList()
    }
}