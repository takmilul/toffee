package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.RechargeByBkashRequest
import com.banglalink.toffee.data.network.response.RechargeByBkashBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class RechargeByBkashService @Inject constructor(private val toffeeApi: ToffeeApi) {
    
    suspend fun execute(request: RechargeByBkashRequest): RechargeByBkashBean? {
        
        val response = tryIO {
            toffeeApi.getRechargeByBkashUrl(
                request
            )
        }

        return response.response
    }
}