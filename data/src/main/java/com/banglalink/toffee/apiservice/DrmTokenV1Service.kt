package com.banglalink.toffee.apiservice

import android.util.Base64
import com.banglalink.toffee.data.network.request.DrmTokenRequest
import com.banglalink.toffee.data.network.request.DrmTokenV1Request
import com.banglalink.toffee.data.network.response.DrmTokenV1Response
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Log
import javax.inject.Inject

class DrmTokenV1Service
@Inject constructor(
    private val pref: SessionPreference,
    private val toffeeApi: ToffeeApi) {
    suspend fun execute(contentId: String): String? {
        Log.i("DRM_T", "Requesting token for -> $contentId")

        val response = tryIO {
            toffeeApi.getDrmTokenV1(
                contentId,
                DrmTokenV1Request(
                    pref.customerId,
                    pref.password,

                )
            )
        }
        return response.response?.drmTokenV1
    }
}