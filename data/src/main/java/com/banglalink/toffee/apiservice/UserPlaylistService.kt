package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelUserPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelPlaylist
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class UserPlaylistService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val channelOwnerId: Int
) : BaseApiService<MyChannelPlaylist> {

    override suspend fun loadData(offset: Int, limit: Int): List<MyChannelPlaylist> {
        
        val isOwner = if (preference.customerId == channelOwnerId) 1 else 0
        val response = tryIO {

            toffeeApi.getMyChannelUserPlaylist(
                isOwner,
                channelOwnerId,
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_USER_CHANNEL_PLAYLISTS),
                MyChannelUserPlaylistRequest(preference.customerId, preference.password)
            )
        }
        
        return response.response.channelPlaylist ?: emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(channelOwnerId: Int): UserPlaylistService
    }
}