package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelDetailRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelDetailBean
import com.banglalink.toffee.util.getFormattedViewsText
import javax.inject.Inject

class MyChannelGetDetailService @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(isOwner: Int, isPublic:Int, channelId: Int, channelOwnerId: Int): MyChannelDetailBean {

        val response = tryIO2 {
            toffeeApi.getMyChannelDetails(
                channelOwnerId,
                isOwner,
//                isPublic,
                channelId,
                preference.getDBVersionByApiName("getUgcChannelDetails"),
                MyChannelDetailRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        response.response.apply {
            this.formattedSubscriberCount = getFormattedViewsText(subscriberCount.toString())
        }
        
        return response.response
    }
}