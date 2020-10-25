package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistCreateRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelPlaylistCreateBean
import javax.inject.Inject

class MyChannelPlaylistCreateService @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(isOwner: Int, channelId: Int, playlistName: String): MyChannelPlaylistCreateBean {

        val response = tryIO2 {
            toffeeApi.createMyChannelPlaylist(
                MyChannelPlaylistCreateRequest(
                    preference.customerId,
                    preference.password,
                    channelId,
                    isOwner,
                    playlistName
                )
            )
        }

        return response.response
    }
}