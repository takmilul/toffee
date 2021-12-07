package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.SubscribedUserChannelsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.UserChannelInfo
import javax.inject.Inject

class SubscribedUserChannelsService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference
): BaseApiService<UserChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<UserChannelInfo> {
        
        val request =  SubscribedUserChannelsRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getSubscribedUserChannels(
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcChannelSubscriptionList"),
                request
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map { 
                localSync.syncSubscribedUserChannels(it)
                it
            }
        } else emptyList()
    }
}