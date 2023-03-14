package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.GrantTokenRequest
import com.banglalink.toffee.data.network.response.GrantTokenResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class BkashGrandTokenService @Inject constructor(private val mPref: SessionPreference, private val api: ExternalApi) {
    
    suspend fun execute(): GrantTokenResponse {
        return tryIOExternal {
            api.grantToken(
                mPref.bkashGrantTokenUrl,
                mPref.bkashUsername,
                mPref.bkashPassword,
                GrantTokenRequest(
                    mPref.bkashAppKey,
                    mPref.bkashAppSecret
                )
            )
        }
    }
}