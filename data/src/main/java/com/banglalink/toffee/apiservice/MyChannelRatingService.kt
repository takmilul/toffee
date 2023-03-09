package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelRatingRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelRatingBean
import javax.inject.Inject

class MyChannelRatingService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {
    
    suspend fun execute(channelOwnerId: Int, rating: Float): MyChannelRatingBean{
        val response = tryIO {
            toffeeApi.rateMyChannel(
                MyChannelRatingRequest(channelOwnerId, rating, channelOwnerId, preference.customerId, preference.password)
            )
        }

        return response.response
    }
}