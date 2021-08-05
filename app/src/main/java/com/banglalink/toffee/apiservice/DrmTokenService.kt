package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.DrmTokenRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class DrmTokenService  @Inject constructor(private val pref: SessionPreference, private val toffeeApi: ToffeeApi) {
    suspend fun execute(contentId: String): String? {
        val response = tryIO2 {
            toffeeApi.getDrmToken(
                DrmTokenRequest(contentId, pref.customerId)
            )
        }
        return response.response
    }
}