package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.AssistedInject

class GetChannelWithCategoryPaging  @AssistedInject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
): BaseApiService<ChannelInfo> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getChannels(
                preference.getDBVersionByApiName(ApiNames.GET_ALL_TV_CHANNELS),
                AllChannelRequest(
                    0,
                    preference.customerId,
                    preference.password
                )
            )
        }
        val finalList = mutableListOf<ChannelInfo>()
        response.response.channelCategoryList.forEach{
            it.channels?.filter {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                localSync.syncData(it, isFromCache = response.isFromCache)
                if (!it.isExpired) {
                    finalList.add(it)
                }
                !it.isExpired
            }
        }
        return finalList
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(): GetChannelWithCategoryPaging
    }
}