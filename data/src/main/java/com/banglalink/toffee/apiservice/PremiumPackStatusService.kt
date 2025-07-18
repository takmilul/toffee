package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PremiumPackStatusRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ActivePack
import javax.inject.Inject

class PremiumPackStatusService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference
) {
    
    suspend fun loadData(contentId: Int, packId: Int): List<ActivePack> {
        val request = PremiumPackStatusRequest(
            preference.customerId, preference.password, packId, if (preference.isPrepaid) 1 else 0
        )
        val isBlNumber = if (preference.isBanglalinkNumber == "true") 1 else 0
        
        val response = tryIO {
            toffeeApi.getPremiumStatus(
                isBlNumber, contentId, preference.getDBVersionByApiName(ApiNames.PREMIUM_DATA_PACK_STATUS), request
            )
        }
        return response.response?.loginRelatedSubsHistory ?: emptyList()
    }
}