package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelVideoDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelDeleteVideoBean
import javax.inject.Inject

class MyChannelVideoDeleteService @Inject constructor(
    private val mPref: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(contentId: Int): MyChannelDeleteVideoBean {
        val response = tryIO2 {
            toffeeApi.deleteMyChannelVideo(
                MyChannelVideoDeleteRequest(
                    contentId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}