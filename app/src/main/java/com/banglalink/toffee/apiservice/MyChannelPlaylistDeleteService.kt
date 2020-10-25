package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelDeletePlaylistBean
import javax.inject.Inject

class MyChannelPlaylistDeleteService @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(playListId: Int): MyChannelDeletePlaylistBean {
        val response = tryIO2 {
            toffeeApi.deleteMyChannelPlaylist(
                MyChannelPlaylistDeleteRequest(
                    playListId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}