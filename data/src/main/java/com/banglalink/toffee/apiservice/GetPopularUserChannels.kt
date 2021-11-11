package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.PopularChannelsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.UserChannelInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class GetPopularUserChannels @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: ApiCategoryRequestParams,
) : BaseApiService<UserChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<UserChannelInfo> {
        
        val request = PopularChannelsRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getUgcPopularChannels(
                requestParams.isCategory,
                requestParams.categoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcPopularChennel"),
                request
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                localSync.syncUserChannel(it)
                it
            }
        } else emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: ApiCategoryRequestParams): GetPopularUserChannels
    }
}