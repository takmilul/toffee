package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.DramaEpisodesBySeasonRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable

@Serializable
data class DramaSeasonRequestParam(
    val type: String,
    val serialSummaryId: Int,
    val seasonNo: Int,
)

class GetDramaEpisodesBySeason @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: DramaSeasonRequestParam,
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getDramaEpisodsBySeason(
                requestParams.type,
                requestParams.serialSummaryId,
                requestParams.seasonNo,
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_WEB_SERIES_BY_SEASON),
                DramaEpisodesBySeasonRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.filter {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                localSync.syncData(it, isFromCache = response.isFromCache)
                !it.isExpired
            }
        } else emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: DramaSeasonRequestParam): GetDramaEpisodesBySeason
    }
}