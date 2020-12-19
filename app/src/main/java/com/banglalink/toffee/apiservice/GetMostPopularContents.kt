package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.MostPopularContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class GetMostPopularContents @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        if(offset > 0)  return emptyList()
        val response = tryIO2 {
            toffeeApi.getUgcMostPopularContents(
                "VOD",
                preference.getDBVersionByApiName("getUgcMostPopularContents"),
                MostPopularContentRequest(
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
}