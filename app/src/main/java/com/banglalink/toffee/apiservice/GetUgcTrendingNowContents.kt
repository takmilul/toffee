package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.AllUserChannelsEditorsChoiceRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.EditorsChoiceFeaturedRequestParams
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class ApiCategoryRequestParams(
    val type: String,
    val isCategory: Int,
    val categoryId: Int,
)

class GetUgcTrendingNowContents @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: EditorsChoiceFeaturedRequestParams,
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        if (offset > 0) return emptyList()
        val request = AllUserChannelsEditorsChoiceRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getUgcEditorsChoice(
                requestParams.type,
                requestParams.pageType.value,
                requestParams.categoryId,
                preference.getDBVersionByApiName("getUgcCategoryEditorChoice"),
                request
            )
        }
        return if (response.response.channels != null) {
            response.response.channels.filter {
                try {
                    Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
                } catch (e: Exception) {
                    true
                }
            }.map {
                it.categoryId = requestParams.categoryId
                localSync.syncData(it)
                it
            }
        } else emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: EditorsChoiceFeaturedRequestParams): GetUgcTrendingNowContents
    }
}