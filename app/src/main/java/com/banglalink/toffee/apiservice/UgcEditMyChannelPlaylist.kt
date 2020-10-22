package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UgcEditMyChannelPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcEditPlaylistBean
import javax.inject.Inject

class UgcEditMyChannelPlaylist @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(playlistId: Int, playlistName: String): UgcEditPlaylistBean {

        val response = tryIO2 {
            toffeeApi.ugcEditMyChannelPlaylist(
                UgcEditMyChannelPlaylistRequest(
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