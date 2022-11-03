package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ContentShareableRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class GetContentFromShareableUrl @Inject constructor (private val preference: SessionPreference, private val toffeeApi: ToffeeApi){
    
    suspend fun execute(videoUrl:String, type: String?): ChannelInfo?{
        val response = tryIO2 {
            toffeeApi.getContentFromShareableUrl(ContentShareableRequest(videoUrl, preference.customerId, preference.password, type))
        }
        
        return if(response.response.channels==null) null else response.response.channels[0].apply {
            isFromSportsCategory = (isVOD && categoryId == 16)
        }
    }
}