package com.banglalink.toffee.apiservice

import android.util.Base64
import android.util.Log
import com.banglalink.toffee.data.network.request.DrmTokenRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class DrmTokenService  @Inject constructor(private val pref: SessionPreference, private val toffeeApi: ToffeeApi) {
    suspend fun execute(contentId: String, duration: Int = 2_592_000): String? {
        Log.e("DRM_T", "Requesting token for -> $contentId")
        val base64 = Base64.encodeToString(pref.customerId.toString().toByteArray(), Base64.NO_WRAP).reversed()
        val response = tryIO2 {
            toffeeApi.getDrmToken(
                "${pref.drmTokenUrl!!}/drm-token",
                base64,
                DrmTokenRequest(contentId, pref.customerId.toString(), pref.password, licenseDuration = duration)
            )
        }
        return response.response?.drmToken
    }
}