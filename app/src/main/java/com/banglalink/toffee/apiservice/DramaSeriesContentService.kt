package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.DramaSeriesContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class DramaSeriesContentService @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ChannelRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getDramaSeriesContents(
                requestParams.type,
                requestParams.subcategoryId,
                limit,
                offset,
                preference.getDBVersionByApiName("getContentsV5"),
                DramaSeriesContentRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response.channels ?: emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): DramaSeriesContentService
    }
}