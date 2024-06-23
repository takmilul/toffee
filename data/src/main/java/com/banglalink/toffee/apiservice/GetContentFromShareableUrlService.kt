package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.ContentShareableRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class GetContentFromShareableUrlService @Inject constructor (
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
) {
    
    suspend fun execute(videoUrl:String, type: String?): ChannelInfo?{
        val response = tryIO {
            toffeeApi.getContentFromShareableUrl(ContentShareableRequest(videoUrl, preference.customerId, preference.password, type))
        }
        
        return if(response.response?.channels == null) null else response.response.channels[0].apply {
            isFromSportsCategory = (categoryId == 16)
            localSync.syncData(this, isFromCache = response.isFromCache)
        }
    }
}