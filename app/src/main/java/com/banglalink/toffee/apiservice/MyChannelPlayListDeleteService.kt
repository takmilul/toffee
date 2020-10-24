package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelPlaylistDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.DeletePlayListBean
import javax.inject.Inject

class MyChannelPlayListDeleteService @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(playListId: Int): DeletePlayListBean {
        val response = tryIO2 {
            toffeeApi.deleteMyChannelPlayList(
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