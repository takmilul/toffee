package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.AllUserChannelsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.UserChannelInfo
import javax.inject.Inject

class AllUserChannelsService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync
): BaseApiService<UserChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<UserChannelInfo> {
        
        val request =  AllUserChannelsRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getAllUserChannels(
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcAllUserChannel"),
                request
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map { 
                localSync.syncUserChannel(it)
                it
            }
        } else emptyList()
    }
}