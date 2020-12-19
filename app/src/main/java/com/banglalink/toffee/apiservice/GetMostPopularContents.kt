package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.MostPopularContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class TrendingNowRequestParam(
    val type: String,
    val categoryId: Int,
    val subCategoryId: Int,
    val isDramaSeries: Boolean = false,
)

class GetMostPopularContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: TrendingNowRequestParam
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getUgcMostPopularContents(
                requestParams.type,
                if(requestParams.isDramaSeries) 1 else 0,
                requestParams.categoryId,
                requestParams.subCategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcMostPopularContents"),
                MostPopularContentRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response.channels ?: emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: TrendingNowRequestParam): GetMostPopularContents
    }
}