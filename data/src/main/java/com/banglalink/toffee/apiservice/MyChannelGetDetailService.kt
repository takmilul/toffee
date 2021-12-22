package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelDetailRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelDetailBean
import com.banglalink.toffee.util.getFormattedViewsText
import javax.inject.Inject

class MyChannelGetDetailService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
    private val subscriptionInfoRepository: SubscriptionInfoRepository,
    private val subscriptionCountRepository: SubscriptionCountRepository,
) {

    suspend fun execute(channelOwnerId: Int): MyChannelDetailBean {
        val isOwner = if (preference.customerId == channelOwnerId) 1 else 0
        
        val response = tryIO2 {
            toffeeApi.getMyChannelDetails(
                channelOwnerId,
                isOwner,
                isOwner.xor(1),
                channelOwnerId,
                preference.getDBVersionByApiName(ApiNames.GET_UGC_MY_CHANNEL_DETAILS),
                MyChannelDetailRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
    
        return response.response.apply {
            formattedSubscriberCount = getFormattedViewsText(subscriberCount.toString())
            subscriberCount = subscriptionCountRepository.getSubscriberCount(channelOwnerId)
            isSubscribed = if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(channelOwnerId, preference.customerId) != null) 1 else 0
        }
    }
}