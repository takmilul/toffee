package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.TrendingChannelsRequest
import com.banglalink.toffee.data.network.request.UgcPopularChannelsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.TrendingChannelInfo
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import javax.inject.Inject

class TrendingChannelsService @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
): BaseApiService<TrendingChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<TrendingChannelInfo> {
        if(offset > 0) return emptyList()
        val request =  TrendingChannelsRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getTrendingChannels(
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcAllUserChannel"),
                request
            )
        }

        if (response.response.channels != null) {
            return response.response.channels
        }

        return emptyList()
    }
}