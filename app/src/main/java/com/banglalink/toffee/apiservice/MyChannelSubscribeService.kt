package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelSubscribeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelSubscribeBean
import javax.inject.Inject

class MyChannelSubscribeService @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(channelId: Int, subStatus: Int): MyChannelSubscribeBean {
        val response = tryIO2 {
            toffeeApi.subscribeOnMyChannel(
                MyChannelSubscribeRequest(
                    channelId,
                    subStatus,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}