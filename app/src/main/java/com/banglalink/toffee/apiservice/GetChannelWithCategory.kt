package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelCategory
import javax.inject.Inject

class GetChannelWithCategory @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(subcategoryId: Int): List<ChannelCategory> {
        val response = tryIO2 {
            toffeeApi.getChannels(
                preference.getDBVersionByApiName("getAppHomePageContentTofeeV2"),
                AllChannelRequest(
                    subcategoryId,
                    preference.customerId,
                    preference.password
                )
            )
        }
        return response.response.channelCategoryList
    }
}