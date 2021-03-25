package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

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
        val response = tryIO2 {
            toffeeApi.getMyChannelPlaylistVideos(
                requestParams.channelOwnerId,
                isOwner,
                requestParams.playlistId,
                limit, offset,
                preference.getDBVersionByApiName("getUgcContentByPlaylist"),
                MyChannelPlaylistVideosRequest(preference.customerId, preference.password)
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                localSync.syncData(it)
                it
            }
        } else emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): MyChannelPlaylistVideosService
    }
}