package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentShareableRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo

class GetContentFromShareableUrl (private val preference: Preference,private val toffeeApi: ToffeeApi){

    suspend fun execute(videoUrl:String): ChannelInfo?{
        val response = tryIO2 {
            toffeeApi.getContentFromShareableUrl(ContentShareableRequest(videoUrl,preference.customerId,preference.password))
        }

        return if(response.response.channels==null) null else response.response.channels[0]
    }
}