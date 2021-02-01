package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class GetHistory @Inject constructor(
        private val preference: Preference,
        private val toffeeApi: ToffeeApi,
        private val viewCountDAO: ViewCountDAO,
        private val viewProgressRepo: ContentViewPorgressRepsitory,
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getHistoryContents(
                HistoryContentRequest(
                    preference.customerId,
                    preference.password,
                    offset = offset,
                    limit = limit
                )
            )
        }

        return if (response.response.channels != null) {
            response.response.channels.map {
                val viewCount = viewCountDAO.getViewCountByChannelId(it.id.toInt())
                if(viewCount!=null){
                    it.view_count= viewCount.toString()
                }
                it.viewProgress = viewProgressRepo.getProgressByContent(it.id.toLong())?.progress ?: 0L
                it
            }
        } else emptyList()
    }
}