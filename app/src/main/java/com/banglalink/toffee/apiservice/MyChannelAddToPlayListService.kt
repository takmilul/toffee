package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelAddToPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.AddToPlayListBean
import javax.inject.Inject

class MyChannelAddToPlayListService @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend fun invoke(playListId: Int, contentId: Int): AddToPlayListBean {
        val response = tryIO2 {
            toffeeApi.addToMyChannelPlayList(
                MyChannelAddToPlaylistRequest(
                    playListId,
                    contentId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}