package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.MostPopularContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

data class LandingUserChannelsRequestParam(
    val type: String,
    val categoryId: Int,
    val subCategoryId: Int,
    val isDramaSeries: Boolean = false,
)

class GetMostPopularContents @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: LandingUserChannelsRequestParam,
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getUgcMostPopularContents(
                requestParams.type,
                if (requestParams.isDramaSeries) 1 else 0,
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

        return if (response.response.channels != null) {
            response.response.channels.map {
                localSync.syncData(it)
                it
            }
        } else emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: LandingUserChannelsRequestParam): GetMostPopularContents
    }
}