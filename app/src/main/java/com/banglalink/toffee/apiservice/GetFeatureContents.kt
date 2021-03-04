package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.EditorsChoiceFeaturedRequestParams
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class GetFeatureContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: EditorsChoiceFeaturedRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        if(offset > 0) return emptyList()
        val request =  FeatureContentRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getUgcFeatureContents(
                requestParams.type,
                requestParams.pageType.value,
                requestParams.categoryId,
                preference.getDBVersionByApiName("getUgcFeatureCategoryContents"),
                request
            )
        }

        return response.response.channels?.map {
            localSync.syncData(it)
            it
        } ?: emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: EditorsChoiceFeaturedRequestParams): GetFeatureContents
    }
}