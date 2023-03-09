package com.banglalink.toffee.apiservice

import com.banglalink.toffee.Constants
import com.banglalink.toffee.Constants.BKASH_APP_KEY
import com.banglalink.toffee.Constants.BKASH_APP_SECRET
import com.banglalink.toffee.data.network.request.GrantTokenBodyRequest
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
                Constants.BKASH_USER_NAME,
                Constants.BKASH_PASSWORD,
                GrantTokenBodyRequest(
                    BKASH_APP_KEY,
                    BKASH_APP_SECRET
                )
            )
        }
    }
}