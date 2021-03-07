package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelAddToPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import javax.inject.Inject

class MyChannelAddToPlayListService @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend fun invoke(playListId: Int, contentId: Int, channelOwnerId: Int): MyChannelAddToPlaylistBean {
        val isOwner = if (mPref.customerId == channelOwnerId) 1 else 0
        val response = tryIO2 {
            toffeeApi.addToMyChannelPlayList(
                MyChannelAddToPlaylistRequest(
                    playListId,
                    contentId,
                    channelOwnerId,
                    isOwner,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}