package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn

class VerifyCode(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(code:String,regSessionToken:String,referralCode:String = ""):CustomerInfoSignIn{
        val response = tryIO { toffeeApi.verifyCode(getRequest(code,regSessionToken,referralCode)) }
        preference.customerId = response.response.customerId
        preference.customerName = response.response.customerName?:""
        preference.sessionToken = response.response.sessionToken?:""
        preference.password = response.response.password?:""
        preference.balance = response.response.balance
        if(response.response.dbVersion!=null){
            preference.setDBVersion(response.response.dbVersion!!)
        }
       

        return response.response
    }

    private fun getRequest(code:String,regSessionToken:String,referralCode:String):VerifyCodeRequest{
        return VerifyCodeRequest(code,regSessionToken,referralCode,preference.fcmToken,preference.latitude,preference.longitude)
    }
}