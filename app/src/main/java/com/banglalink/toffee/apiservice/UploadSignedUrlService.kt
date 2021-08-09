package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UploadSignedUrlRequest
import com.banglalink.toffee.data.network.response.UploadSignedUrlResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class UploadSignedUrlService @Inject constructor(private val pref: SessionPreference, private val api: ToffeeApi
) {

    suspend fun execute(url: String): UploadSignedUrlResponse {
        return tryIO2 {
            api.uploadSignedUrl(
                 UploadSignedUrlRequest(
                    pref.customerId,
                    pref.password,
                    url
                )
            )
        }
    }
}