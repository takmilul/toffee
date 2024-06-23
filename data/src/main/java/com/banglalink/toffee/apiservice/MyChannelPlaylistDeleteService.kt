package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelDeletePlaylistBean
import javax.inject.Inject

class MyChannelPlaylistDeleteService @Inject constructor(
    private val mPref: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(playListId: Int, isUserPlaylist: Int): MyChannelDeletePlaylistBean? {
        val response = tryIO {
            toffeeApi.deleteMyChannelPlaylist(
                MyChannelPlaylistDeleteRequest(
                    mPref.customerId,
                    mPref.password,
                    playListId,
                    isUserPlaylist
                )
            )
        }
        return response.response
    }
}