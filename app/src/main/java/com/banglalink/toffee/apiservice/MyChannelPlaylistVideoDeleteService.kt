package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideoDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelDeletePlaylistVideoBean
import javax.inject.Inject

class MyChannelPlaylistVideoDeleteService @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(channelId: Int, playlistContentId: Int, playlistId: Int): MyChannelDeletePlaylistVideoBean {
        val response = tryIO2 {
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