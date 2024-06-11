package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelSubscribeRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelSubscribeBean
import javax.inject.Inject

class MyChannelSubscribeService @Inject constructor(
    private val mPref: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(channelId: Int, subStatus: Int, channelOwnerId: Int): MyChannelSubscribeBean? {
        val response = tryIO {
            toffeeApi.subscribeOnMyChannel(
                MyChannelSubscribeRequest(
                    channelId,
                    subStatus,
                    channelOwnerId,
                    mPref.customerId,
                    mPref.password
                )
            )
        }
        return response.response
    }
}