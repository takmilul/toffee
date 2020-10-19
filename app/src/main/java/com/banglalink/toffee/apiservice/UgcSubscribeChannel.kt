package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UgcFollowCategoryRequest
import com.banglalink.toffee.data.network.request.UgcSubscribeChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.FollowCategoryBean
import com.banglalink.toffee.model.SubscribeChannelBean
import javax.inject.Inject

class UgcSubscribeChannel @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(channelId: Int, subStatus: Int): SubscribeChannelBean {
        val response = tryIO2 {
            toffeeApi.subscribeOnChannel(
                UgcSubscribeChannelRequest(
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