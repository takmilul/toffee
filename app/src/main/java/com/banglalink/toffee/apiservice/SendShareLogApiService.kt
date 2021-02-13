package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentShareLogRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ContentShareLogBean
import javax.inject.Inject

class SendShareLogApiService @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun execute(contentId: Int, videoUrl: String?): ContentShareLogBean {
        val response = tryIO2 {
            toffeeApi.sendShareLog(
                ContentShareLogRequest(contentId, preference.customerId, preference.password, videoUrl)
            )
        }
        return response.response
    }
}