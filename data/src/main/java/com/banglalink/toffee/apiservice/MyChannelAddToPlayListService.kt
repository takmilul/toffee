package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelAddToPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import javax.inject.Inject

class MyChannelAddToPlayListService @Inject constructor(
    private val mPref: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend fun invoke(
        playListId: Int,
        contentId: Int,
        channelOwnerId: Int,
        isUserPlaylist:Int
    ): MyChannelAddToPlaylistBean? {
        
        val isOwner = if (mPref.customerId == channelOwnerId) 1 else 0
        val response = tryIO {
            toffeeApi.addToMyChannelPlayList(
                MyChannelAddToPlaylistRequest(
                    playListId,
                    contentId,
                    channelOwnerId,
                    isOwner,
                    mPref.customerId,
                    mPref.password,
                    isUserPlaylist
                )
            )
        }
        return response.response
    }
}