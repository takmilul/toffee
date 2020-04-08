package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.VerifyCodeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn

class VerifyCode(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(code:String,regSessionToken:String,referralCode:String = ""):CustomerInfoSignIn{
        val response = tryIO { toffeeApi.verifyCode(getRequest(code,regSessionToken,referralCode)) }

        response.customerInfoSignIn.also {
            preference.balance = it.balance
            preference.customerId = it.customerId
            preference.password = it.password?:""
            preference.customerName = it.customerName?:""
            if(it.dbVersion!=null)
                preference.setDBVersion(it.dbVersion!!)
            preference.sessionToken = (it.sessionToken?:"")

            preference.setHeaderSessionToken(it.headerSessionToken)
            preference.setHlsOverrideUrl(it.hlsOverrideUrl)
            preference.setShouldOverrideHlsUrl(it.hlsUrlOverride)
            preference.setSessionTokenLifeSpanInMillis(it.tokenLifeSpan.toLong() * 1000 * 3600)
            if(it.isBanglalinkNumber!=null){
                preference.isBanglalinkNumber = it.isBanglalinkNumber
            }
        }
       

        return response.customerInfoSignIn
    }

    private fun getRequest(code:String,regSessionToken:String,referralCode:String):VerifyCodeRequest{
        return VerifyCodeRequest(code,regSessionToken,referralCode,preference.fcmToken,preference.latitude,preference.longitude)
    }
}