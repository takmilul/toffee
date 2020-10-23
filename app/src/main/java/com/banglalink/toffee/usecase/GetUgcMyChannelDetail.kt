package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.UgcMyChannelDetailRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcMyChannelDetailBean
import com.banglalink.toffee.util.getFormattedViewsText
import javax.inject.Inject

class GetUgcMyChannelDetail @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(isOwner: Int, channelId: Int): UgcMyChannelDetailBean {

        val response = tryIO2 {
            toffeeApi.getUgcMyChannelDetails(
                isOwner,
                channelId,
                preference.getDBVersionByApiName("getUgcChannelDetails"),
                UgcMyChannelDetailRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        response.response.apply {
            this.subscriberCount = getFormattedViewsText(subscriberCount.toString())
        }
        
        return response.response
    }
}