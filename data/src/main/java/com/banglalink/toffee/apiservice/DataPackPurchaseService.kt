package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.request.PremiumPackStatusRequest
import com.banglalink.toffee.data.network.response.PremiumPackStatusResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ActivePack
import javax.inject.Inject

class DataPackPurchaseService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference
) {
    
    suspend fun loadData(packId:Int?,packTitle:String?,contentList:List<Int>?,paymentMethodId:Int?,packCode:String?,packDetails:String?,packPrice:Int?,packDuration:Int?)
    : PremiumPackStatusResponse.PremiumPackStatusBean {
       
        val isBlNumber = if (preference.isBanglalinkNumber == "true") 1 else 0
        val request = DataPackPurchaseRequest(
            customerId=preference.customerId,
            password=preference.password,
            isBanglalinkNumber = isBlNumber,
            packId = packId,
            packTitle = packTitle,
            contents = contentList,
            paymentMethodId = paymentMethodId,
            packCode = packCode,
            packDetails = packDetails,
            packPrice = packPrice,
            packDuration = packDuration,
        )
        val response = tryIO2 {
            toffeeApi.purchaseDataPack(
                 request
            )
        }
        return response.response
    }
}