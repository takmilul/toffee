package com.banglalink.toffee.apiservice

import com.banglalink.toffee.Constants.PLAY_CDN
import com.banglalink.toffee.data.network.request.MediaCdnSignUrlRequest
import com.banglalink.toffee.data.network.response.MediaCdnSignUrl
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class MediaCdnSignUrlService  @Inject constructor(private val pref: SessionPreference, private val toffeeApi: ToffeeApi) {
    suspend fun execute(contentId: String): MediaCdnSignUrl? {
        val response = tryIO2 {
            toffeeApi.getMediaCdnSignUrl(
                MediaCdnSignUrlRequest(
                    pref.customerId.toString(),
                    pref.password,
                    contentId,
                    PLAY_CDN,
                    pref.getDBVersionByApiName(ApiNames.MEDIA_CDN_SIGN_URL)
                )
            )
        }
        return response.response
    }
}