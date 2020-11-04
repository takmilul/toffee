package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.MyChannelPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelPlaylist
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelPlaylistParams(val isOwner: Int, val channelOwnerId: Int)

class MyChannelPlaylistService @AssistedInject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, @Assisted private val isOwner: Int, @Assisted private val channelOwnerId: Int):
    BaseApiService<MyChannelPlaylist> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<MyChannelPlaylist> {
        if(offset > 0) return emptyList()
        val response = tryIO2 {

            Log.i("UGC_Playlist_Service", "UGC_API -- isOwner: ${isOwner}, ownerId: ${channelOwnerId}")
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

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(isOwner: Int, channelOwnerId: Int): MyChannelPlaylistService
    }
}