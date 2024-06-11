package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MyChannelUserPlaylistVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class UserPlaylistVideosService @AssistedInject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    @Assisted private val requestParams: MyChannelPlaylistContentParam,
) : BaseApiService<ChannelInfo> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val isOwner = if (preference.customerId == requestParams.channelOwnerId) 1 else 0
        val response = tryIO {
            toffeeApi.getMyChannelUserPlaylistVideos(
                requestParams.channelOwnerId,
                isOwner,
                requestParams.playlistId,
                limit, offset,
                preference.getDBVersionByApiName(ApiNames.GET_USER_CHANNEL_PLAYLIST_VIDEOS),
                MyChannelUserPlaylistVideosRequest(preference.customerId, preference.password)
            )
        }
        
        return response.response?.channels?.filter {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            localSync.syncData(it, isFromCache = response.isFromCache)
            !it.isExpired
        } ?: emptyList()
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): UserPlaylistVideosService
    }
}