package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.AutoRenewRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference

class SetAutoRenew(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(packageId:Int,autoRenew:Boolean):String{
        val response = tryIO2 {
            toffeeApi.setAutoRenew(AutoRenewRequest(packageId,preference.customerId,preference.password,if(autoRenew) "true" else "false"))
        }
        return response.response.message!!
    }
}