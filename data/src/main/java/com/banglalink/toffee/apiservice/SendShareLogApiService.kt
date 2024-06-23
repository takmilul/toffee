package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentShareLogRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ContentShareLogBean
import javax.inject.Inject

class SendShareLogApiService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun execute(contentId: Int, videoUrl: String?): ContentShareLogBean? {
        val response = tryIO {
            toffeeApi.sendShareLog(
                ContentShareLogRequest(contentId, preference.customerId, preference.password, videoUrl)
            )
        }
        return response.response
    }
}