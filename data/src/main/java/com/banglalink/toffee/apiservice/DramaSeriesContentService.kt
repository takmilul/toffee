package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.DramaSeriesContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class DramaSeriesContentService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: ChannelRequestParams
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getDramaSeriesContents(
                requestParams.type,
                requestParams.subcategoryId,
                requestParams.isFilter,
                requestParams.hashTag,
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_WEB_SERIES_CONTENT),
                DramaSeriesContentRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
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