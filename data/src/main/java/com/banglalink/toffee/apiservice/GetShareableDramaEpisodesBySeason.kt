package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.DramaEpisodesBySeasonRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.DramaSeriesContentBean
import com.banglalink.toffee.model.ShareableData
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class GetShareableDramaEpisodesBySeason @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val requestParams: ShareableData,
) {
    
    suspend fun loadData(offset: Int, limit: Int): DramaSeriesContentBean {
        val response = tryIO {
            toffeeApi.getDramaEpisodsBySeason(
                requestParams.contentType,
                requestParams.serialSummaryId ?: 0,
                requestParams.seasonNo ?: 1,
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_WEB_SERIES_BY_SEASON),
                DramaEpisodesBySeasonRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        
        return response.response.apply {
            channels?.map {
                it.activeSeasonList = requestParams.activeSeason
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                localSync.syncData(it)
                it
            }?.filter { !it.isExpired }
        }
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(shareableData: ShareableData): GetShareableDramaEpisodesBySeason
    }
}