package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.CredentialRequest
import com.banglalink.toffee.data.network.response.CredentialResponse
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class CredentialService @Inject constructor(private val pref: SessionPreference, private val authApi: AuthApi) {
    
    suspend fun execute(): CredentialResponse {
        val response = tryIO2 {
            authApi.apiExistingLogin(CredentialRequest(1, pref.fcmToken, pref.hePhoneNumber).also {
                it.isBlNumber = pref.isHeBanglalinkNumber.toString()
            })
        }
        response.credential?.let { customerInfoSignIn ->
            pref.customerId = customerInfoSignIn.customerId
            pref.password = customerInfoSignIn.password!!
        }
        return response
    }
}