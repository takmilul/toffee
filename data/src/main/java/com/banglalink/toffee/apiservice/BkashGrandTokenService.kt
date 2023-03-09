package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.GrantTokenBodyRequest
import com.banglalink.toffee.data.network.response.GrantTokenResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class BkashGrandTokenService @Inject constructor(private val mPref: SessionPreference, private val api: ExternalApi) {
    
    suspend fun execute(): GrantTokenResponse {
        return tryIO2 {
            api.grantToken(
                mPref.bkashGrantTokenUrl,
                mPref.bkashUsername,
                mPref.bkashPassword,
                GrantTokenBodyRequest(
                    mPref.bkashAppKey,
                    mPref.bkashAppSecret
                )
            )
        }
    }
}