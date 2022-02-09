package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistEditRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import javax.inject.Inject

class MyChannelPlaylistEditService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(playlistId: Int, playlistName: String, channelOwnerId: Int, isUserPlaylist: Int): MyChannelPlaylistEditBean {
        val isOwner = if (preference.customerId == channelOwnerId) 1 else 0
        val response = tryIO2 {
            toffeeApi.editMyChannelPlaylist(
                MyChannelPlaylistEditRequest(
                    preference.customerId,
                    preference.password,
                    playlistId,
                    playlistName,
                    channelOwnerId,
                    isOwner,
                    isUserPlaylist
                )
            )
        }

        return response.response
    }
}