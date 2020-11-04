package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.UgcFeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class GetFeatureContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ApiCategoryRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        if(offset > 0) return emptyList()
        val request =  UgcFeatureContentRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getUgcFeatureContents(
                requestParams.type,
                requestParams.isCategory,
                requestParams.categoryId,
                preference.getDBVersionByApiName("getUgcFeatureCategoryContents"),
                request
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = Utils.getFormattedViewsText(it.view_count)
                it
            }
        }

        return emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: ApiCategoryRequestParams): GetFeatureContents
    }
}