package com.banglalink.toffee.apiservice

import android.util.Base64
import com.banglalink.toffee.data.network.request.DrmTokenRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Log
import javax.inject.Inject

class DrmTokenService  @Inject constructor(private val pref: SessionPreference, private val toffeeApi: ToffeeApi) {
    suspend fun execute(contentId: String, duration: Int = 2_592_000, drmType: String = "Widevine"): String? {
        Log.i("DRM_T", "Requesting token for -> $contentId")
        val base64 = Base64.encodeToString(pref.customerId.toString().toByteArray(), Base64.NO_WRAP).reversed()
        val response = tryIO {
            toffeeApi.getDrmToken(
                "${pref.drmTokenUrl!!}/drm-token",
                base64,
                DrmTokenRequest(
                    if(pref.isGlobalCidActive) pref.globalCidName!! else contentId,
                    pref.customerId.toString(),
                    pref.password,
                    drmType,
                    licenseDuration = duration
                )
            )
        }
        return response.response?.drmToken
    }
}