package com.banglalink.toffee.apiservice

import android.util.Base64
import com.banglalink.toffee.data.network.request.DrmLicenseRequest
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIO
import javax.inject.Inject

class DrmLicenseService  @Inject constructor(
    private val externalApi: ExternalApi
) {
    suspend fun execute(
        licenseServerUrl: String,
        payload: String,
        contentId: String = "1",
        packageId: String = "1"
    ): ByteArray? {
        val response = tryIO {
            externalApi.getLicense(
                url = licenseServerUrl,
                request = DrmLicenseRequest(
                    payload = payload,
                    contentId = contentId,
                    packageId = packageId,
                )
            )
        }
        return response.data?.payload?.let {
            Base64.decode(it, Base64.NO_WRAP)
        }
    }
}