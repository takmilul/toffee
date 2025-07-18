package com.banglalink.toffee.apiservice

import com.banglalink.toffee.Constants.DEVICE_ID_HEADER
import com.banglalink.toffee.Constants.HE_SESSION_TOKEN_HEADER
import com.banglalink.toffee.data.network.response.HeaderEnrichmentResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.extension.toMD5
import javax.inject.Inject

class HeaderEnrichmentService @Inject constructor(private val pref: CommonPreference, private val api: ToffeeApi) {

    suspend fun execute(): HeaderEnrichmentResponse {
        val md5 = pref.deviceId.toMD5()
        val midIndex = md5.length / 2
        val sessionToken = md5.substring(midIndex, md5.length) + md5.substring(0, midIndex)
        return tryIO {
            api.getHeaderEnrichment(
                mapOf(
                    DEVICE_ID_HEADER to pref.deviceId,
                    HE_SESSION_TOKEN_HEADER to sessionToken
                )
            )
        }
    }
}