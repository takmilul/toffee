package com.banglalink.toffee.apiservice

import android.util.Base64
import com.banglalink.toffee.data.network.request.DrmLicenseRequest
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class DrmLicenseService  @Inject constructor(
    private val pref: SessionPreference,
    private val externalApi: ExternalApi
) {
    suspend fun execute(licenseServerUrl: String, payload: String): ByteArray? {
        val response = tryIO {
            externalApi.getLicense(
                url = licenseServerUrl,
                request = DrmLicenseRequest(
                    payload = payload
                )
            )
        }
        return response.data?.payload?.let {
            Base64.decode(it, Base64.NO_WRAP)
        }
    }
}