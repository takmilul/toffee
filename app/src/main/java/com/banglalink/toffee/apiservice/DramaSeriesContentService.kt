package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.DramaSeriesContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class DramaSeriesContentService @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: ChannelRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getDramaSeriesContents(
                requestParams.type,
                requestParams.subcategoryId,
                requestParams.isFilter,
                requestParams.hashTag,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcLatestDramaSerial"),
                DramaSeriesContentRequest(
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
        fun create(requestParams: ChannelRequestParams): DramaSeriesContentService
    }
}