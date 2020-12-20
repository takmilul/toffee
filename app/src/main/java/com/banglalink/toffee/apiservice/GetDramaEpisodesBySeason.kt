package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.DramaEpisodesBySeasonRequest
import com.banglalink.toffee.data.network.request.DramaSeriesContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class DramaSeasonRequestParam(
    val type: String,
    val serialSummaryId: Int,
    val seasonNo: Int,
)

class GetDramaEpisodesBySeason @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    @Assisted private val requestParams: DramaSeasonRequestParam
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getDramaEpisodsBySeason(
                requestParams.type,
                requestParams.serialSummaryId,
                requestParams.seasonNo,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcDramaSerialBySeason"),
                DramaEpisodesBySeasonRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                it
            }
        } else emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: DramaSeasonRequestParam): GetDramaEpisodesBySeason
    }
}