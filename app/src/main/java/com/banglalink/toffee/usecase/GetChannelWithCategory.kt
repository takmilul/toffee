package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelCategory

class GetChannelWithCategory(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(subcategoryId: Int): List<ChannelCategory> {
        val response = tryIO {
            toffeeApi.getChannels(
                AllChannelRequest(
                    subcategoryId,
                    preference.customerId,
                    preference.password
                )
            )
        }
        return response.response.channelCategoryList;
    }
}