package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.MyChannelPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelPlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelPlaylistParams(val isOwner: Int, val channelId: Int)

class MyChannelPlaylistService @AssistedInject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, @Assisted private val params: MyChannelPlaylistParams):
    BaseApiService<MyChannelPlaylist> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<MyChannelPlaylist> {
        if(offset > 0) return emptyList()
        val response = tryIO2 {
            toffeeApi.getMyChannelPlaylist(
                params.isOwner,
                params.channelId,
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

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(params: MyChannelPlaylistParams): MyChannelPlaylistService
    }
}