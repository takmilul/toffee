package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn

class ApiLogin(private val pref: Preference, private val authApi: AuthApi) {


    suspend fun execute():CustomerInfoSignIn{
        val response = tryIO2 { authApi.apiLogin(getApiLoginRequest()) }
        response.customerInfoSignIn?.let { customerInfoSignIn ->
            pref.balance = (customerInfoSignIn.balance)
            pref.customerId = (customerInfoSignIn.customerId)
            pref.customerName = (customerInfoSignIn.customerName?:"")
            if(customerInfoSignIn.dbVersion!=null)
                pref.setDBVersion(customerInfoSignIn.dbVersion!!)
            pref.sessionToken = (customerInfoSignIn.sessionToken?:"")

            pref.setHeaderSessionToken(customerInfoSignIn.headerSessionToken)
            pref.setHlsOverrideUrl(customerInfoSignIn.hlsOverrideUrl)
            pref.setShouldOverrideHlsUrl(customerInfoSignIn.hlsUrlOverride)
            pref.setSessionTokenLifeSpanInMillis(customerInfoSignIn.tokenLifeSpan.toLong() * 1000 * 3600)

            if(customerInfoSignIn.isBanglalinkNumber!=null){
                pref.isBanglalinkNumber = customerInfoSignIn.isBanglalinkNumber
            }
            customerInfoSignIn.dbVersionList?.let {
                pref.setDBVersion(it)
            }
            pref.latitude = customerInfoSignIn.lat?:""
            pref.longitude = customerInfoSignIn.long?:""
            pref.isSubscriptionActive = customerInfoSignIn.isSubscriptionActive?:"true"
        }
        return response.customerInfoSignIn!!
    }
    private fun getApiLoginRequest() =
        ApiLoginRequest(
            pref.customerId,
            pref.password,
            pref.latitude,
            pref.longitude,
            fcmToken = pref.fcmToken
        )

}