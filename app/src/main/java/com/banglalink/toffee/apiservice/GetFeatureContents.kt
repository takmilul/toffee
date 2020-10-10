package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class GetFeatureContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ChannelRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        if(offset > 0) return emptyList()
        val request =  FeatureContentRequest(
            requestParams.categoryId,
            requestParams.subcategoryId,
            requestParams.type,
            preference.customerId,
            preference.password,
            offset = offset,
            limit = limit
        )

        val response = tryIO2 {
            toffeeApi.getFeatureContentsV2(preference.getDBVersionByApiName("getFeatureContentsV2"),request)
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.category = requestParams.category
                it.subCategoryId = requestParams.subcategoryId
                it.subCategory = requestParams.subcategory
                it
            }
        }

        return emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): GetFeatureContents
    }
}