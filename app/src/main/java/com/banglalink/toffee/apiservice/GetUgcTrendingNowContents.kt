package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.UgcTrendingNowRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class ApiCategoryRequestParams(
    val type: String,
    val isCategory: Int,
    val categoryId: Int
)

class GetUgcTrendingNowContents @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ApiCategoryRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        if(offset > 0) return emptyList()
        val request =  UgcTrendingNowRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getUgcEditorsChoice(
                requestParams.type,
                requestParams.isCategory,
                requestParams.categoryId,
                preference.getDBVersionByApiName("getUgcCategoryEditorChoice"),
                request
            )
        }
        Log.e("TRENDING", response.response.toString())
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }
        }

        return emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: ApiCategoryRequestParams): GetUgcTrendingNowContents
    }
}