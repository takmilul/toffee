package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn

class ApiLogin(private val pref: Preference, private val authApi: AuthApi) {


    suspend fun execute():CustomerInfoSignIn{
        val response = tryIO { authApi.apiLogin(getApiLoginRequest()) }
        response.customerInfoSignIn?.let {
            pref.balance = (it.balance)
            pref.customerId = (it.customerId)
            pref.customerName = (it.customerName?:"")
            if(it.dbVersion!=null)
                pref.setDBVersion(it.dbVersion!!)
            pref.sessionToken = (it.sessionToken?:"")

            pref.setHeaderSessionToken(it.headerSessionToken)
            pref.setHlsOverrideUrl(it.hlsOverrideUrl)
            pref.setShouldOverrideHlsUrl(it.hlsUrlOverride)
            pref.setSessionTokenLifeSpanInMillis(it.tokenLifeSpan.toLong() * 1000 * 3600)
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