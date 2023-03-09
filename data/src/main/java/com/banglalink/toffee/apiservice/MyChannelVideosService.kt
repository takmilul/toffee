package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MyChannelVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class MyChannelVideosRequestParams(
    val type: String,
    val channelOwnerId: Int,
    val categoryId: Int,
    val subcategoryId: Int
)

class MyChannelVideosService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: MyChannelVideosRequestParams,
) :
    BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val isOwner = if (preference.customerId == requestParams.channelOwnerId) 1 else 0
        val response = tryIO {
            toffeeApi.getMyChannelVideos(
                requestParams.type,
                isOwner,
                requestParams.channelOwnerId,
                requestParams.categoryId,
                requestParams.subcategoryId,
                isOwner.xor(1),
                limit, offset,
                preference.getDBVersionByApiName(ApiNames.GET_MY_CHANNEL_ALL_VIDEOS),
                MyChannelVideosRequest(preference.customerId, preference.password, offset, limit)
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                it.isOwner = isOwner == 1
                it.isPublic = isOwner.xor(1) == 1
                localSync.syncData(it)
                it
            }
        }
        return listOf()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: MyChannelVideosRequestParams): MyChannelVideosService
    }
}