package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.response.PremiumPackStatusBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class DataPackPurchaseService @Inject constructor(
    private val toffeeApi: ToffeeApi,
) {
    
    suspend fun loadData(request: DataPackPurchaseRequest) : PremiumPackStatusBean {
        val response = tryIO {
            toffeeApi.purchaseDataPack(request)
        }
        return response.response
    }
}