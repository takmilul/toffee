package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylistContentParam(val channelOwnerId: Int, val playlistId: Int)

class MyChannelPlaylistVideosService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: MyChannelPlaylistContentParam,
) :
    BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val isOwner = if (preference.customerId == requestParams.channelOwnerId) 1 else 0
        val response = tryIO {
            toffeeApi.getMyChannelPlaylistVideos(
                requestParams.channelOwnerId,
                isOwner,
                requestParams.playlistId,
                limit, offset,
                preference.getDBVersionByApiName(ApiNames.GET_MY_CHANNEL_PLAYLIST_VIDEOS),
                MyChannelPlaylistVideosRequest(preference.customerId, preference.password)
            )
        }
        
        return response.response?.channels?.filter {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            it.isOwner = isOwner == 1
            it.isPublic = isOwner.xor(1) == 1
            it.isPlaylist = true
            localSync.syncData(it, isFromCache = response.isFromCache)
            !it.isExpired
        } ?: emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): MyChannelPlaylistVideosService
    }
}