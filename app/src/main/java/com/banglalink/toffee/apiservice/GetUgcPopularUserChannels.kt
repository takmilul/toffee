package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.UgcPopularChannelsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcUserChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class GetUgcPopularUserChannels @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ApiCategoryRequestParams
): BaseApiService<UgcUserChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<UgcUserChannelInfo> {
        if(offset > 0) return emptyList()
        val request =  UgcPopularChannelsRequest(
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

        if (response.response.channels != null) {
            return response.response.channels
        }

        return emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: ApiCategoryRequestParams): GetUgcPopularUserChannels
    }
}