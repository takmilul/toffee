package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideoDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelDeletePlaylistVideoBean
import javax.inject.Inject

class MyChannelPlaylistVideoDeleteService @Inject constructor(
    private val mPref: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(channelId: Int, playlistContentId: Int, playlistId: Int): MyChannelDeletePlaylistVideoBean {
        val response = tryIO {
            toffeeApi.deleteMyChannelPlaylistVideo(
                MyChannelPlaylistVideoDeleteRequest(
                    channelId,
                    playlistContentId,
                    playlistId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}