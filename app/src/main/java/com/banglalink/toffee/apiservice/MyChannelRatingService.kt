package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelRatingRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelRatingBean
import javax.inject.Inject

class MyChannelRatingService @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {
    
    suspend fun execute(channelId: Int, rating: Float, channelOwnerId: Int): MyChannelRatingBean{
        val response = tryIO2 {
            toffeeApi.rateMyChannel(
                MyChannelRatingRequest(channelId, rating, channelOwnerId, preference.customerId, preference.password)
            )
        }

        return response.response
    }
}