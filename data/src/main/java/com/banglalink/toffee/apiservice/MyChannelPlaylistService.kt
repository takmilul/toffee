package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelPlaylist
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class MyChannelPlaylistService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val channelOwnerId: Int
) : BaseApiService<MyChannelPlaylist> {

    override suspend fun loadData(offset: Int, limit: Int): List<MyChannelPlaylist> {
        if (offset > 0) return emptyList()
        val isOwner = if (preference.customerId == channelOwnerId) 1 else 0
        val response = tryIO2 {

            toffeeApi.getMyChannelPlaylist(
                isOwner,
                channelOwnerId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcPlaylistNames"),
                MyChannelPlaylistRequest(preference.customerId, preference.password)
            )
        }

        if (response.response.channelPlaylist != null) {
//            response.response.channelPlaylist.map { it.totalContent = getFormattedViewsText(it.totalContent) }
            return response.response.channelPlaylist
        }
        return emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(channelOwnerId: Int): MyChannelPlaylistService
    }
}