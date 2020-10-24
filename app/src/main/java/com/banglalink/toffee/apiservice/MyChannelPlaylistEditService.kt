package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistEditRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelPlaylistEditBean
import javax.inject.Inject

class MyChannelPlaylistEditService @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(playlistId: Int, playlistName: String): MyChannelPlaylistEditBean {

        val response = tryIO2 {
            toffeeApi.editMyChannelPlaylist(
                MyChannelPlaylistEditRequest(
                    playlistId,
                    playlistName,
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response
    }
}