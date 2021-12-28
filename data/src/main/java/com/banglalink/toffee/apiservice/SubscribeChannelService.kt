package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.request.MyChannelSubscribeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelSubscribeBean
import javax.inject.Inject

class SubscribeChannelService@Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val mPref: SessionPreference,
) {
    
    suspend fun execute(subscriptionInfo: SubscriptionInfo, status: Int): MyChannelSubscribeBean {
        val response = tryIO2 {
            toffeeApi.subscribeOnMyChannel(
                MyChannelSubscribeRequest(
                    subscriptionInfo.channelId,
                    status.takeIf { it == 1 } ?: 0,
                    subscriptionInfo.channelId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response.also { 
            localSync.updateOnSubscribeChannel(it)
        }
    }
}