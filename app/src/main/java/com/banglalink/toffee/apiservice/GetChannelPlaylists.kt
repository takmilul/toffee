package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.UgcMyChannelPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcChannelPlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelPlaylistParams(val isOwner: Int, val channelId: Int)

class GetChannelPlaylists @AssistedInject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, @Assisted private val params: MyChannelPlaylistParams):
    BaseApiService<UgcChannelPlaylist> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<UgcChannelPlaylist> {
        if(offset > 0) return emptyList()
        val response = tryIO2 {
            toffeeApi.getUgcMyChannelPlaylist(
                params.isOwner,
                params.channelId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcPlaylistNames"),
                UgcMyChannelPlaylistRequest(preference.customerId, preference.password)
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
        fun create(params: MyChannelPlaylistParams): GetChannelPlaylists
    }
}