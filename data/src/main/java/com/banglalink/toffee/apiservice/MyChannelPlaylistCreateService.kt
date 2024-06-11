package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistCreateRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import javax.inject.Inject

class MyChannelPlaylistCreateService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(channelOwnerId: Int, playlistName: String, isUserPlaylist: Int ): MyChannelPlaylistCreateBean? {
        val isOwner = if (preference.customerId == channelOwnerId) 1 else 0
        
        val response = tryIO {
            toffeeApi.createMyChannelPlaylist(
                MyChannelPlaylistCreateRequest(
                    preference.customerId,
                    preference.password,
                    channelOwnerId,
                    isOwner,
                    playlistName,
                    isUserPlaylist
                )
            )
        }

        return response.response
    }
}