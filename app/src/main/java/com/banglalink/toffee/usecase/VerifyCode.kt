package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class VerifyCode(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(code:String):Boolean{
        val response = tryIO { toffeeApi.verifyCode(getRequest(code)) }
        preference.customerId = response.response.customerId
        preference.customerName = response.response.customerName?:""
        preference.sessionToken = response.response.sessionToken?:""
        preference.password = response.response.password?:""
        preference.balance = response.response.balance
        if(response.response.dbVersion!=null){
            preference.setDBVersion(response.response.dbVersion!!)
        }
       

        return true
    }

    private fun getRequest(code:String):VerifyCodeRequest{
        return VerifyCodeRequest(code,preference.fcmToken,preference.latitude,preference.longitude)
    }
}