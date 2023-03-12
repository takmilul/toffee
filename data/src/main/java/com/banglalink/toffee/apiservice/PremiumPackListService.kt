package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PremiumPackListRequest
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import javax.inject.Inject

class PremiumPackListService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun loadData(contentId: String): List<PremiumPack> {
        val response = tryIO {
            toffeeApi.getPremiumPackList(
                contentId,
                preference.getDBVersionByApiName(ApiNames.GET_PREMIUM_PACK_LIST),
                PremiumPackListRequest(
                    preference.customerId,
                    preference.password,
                )
            )
        }
        return response.response?.premiumPacks?.map {premiumPack ->
            if (preference.isVerifiedUser) {
                preference.activePremiumPackList.value?.find {
                    it.packId == premiumPack.id && it.isActive && preference.getSystemTime().before(Utils.getDate(it.expiryDate))
                }?.let {
                    runCatching {
                        premiumPack.isPackPurchased = true
                        premiumPack.expiryDate = "Expires on ${Utils.formatPackExpiryDate(it.expiryDate)}"
                        premiumPack.packDetail = if (premiumPack.isAvailableFreePeriod == 1) it.packDetail else "You have bought ${
                            it.packDetail
                        } pack"
                    }
                }
            }
            premiumPack
        } ?: emptyList()
    }
}