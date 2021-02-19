package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.network.request.MostPopularContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
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
        private val localSync: LocalSync,
//        private val viewCountDAO: ViewCountDAO,
//        private val viewProgressRepo: ContentViewPorgressRepsitory,
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

        return if (response.response.channels != null) {
            response.response.channels.map {
                localSync.syncData(it)
//                val viewCount = viewCountDAO.getViewCountByChannelId(it.id.toInt())
//                if(viewCount!=null){
//                    it.view_count= viewCount.toString()
//                }
//                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                it
            }
        } else emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: TrendingNowRequestParam): GetMostPopularContents
    }
}