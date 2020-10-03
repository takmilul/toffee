package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn

class VerifyCode(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(code:String,regSessionToken:String,referralCode:String = ""):CustomerInfoSignIn{
        val response = tryIO2 { toffeeApi.verifyCode(getRequest(code,regSessionToken,referralCode)) }

        response.customerInfoSignIn.also { customerInfoSignIn ->
            preference.saveCustomerInfo(customerInfoSignIn)
        }
       

        return response.customerInfoSignIn
    }

    private fun getRequest(code:String,regSessionToken:String,referralCode:String):VerifyCodeRequest{
        return VerifyCodeRequest(code,regSessionToken,referralCode,preference.fcmToken,preference.latitude,preference.longitude)
    }
}