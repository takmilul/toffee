package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelCategory
import com.google.gson.Gson
import javax.inject.Inject

class GetChannelWithCategory @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val tvChannelRepo: TVChannelRepository
) {
    suspend operator fun invoke(subcategoryId: Int) {
        val response = tryIO2 {
            toffeeApi.getChannels(
                preference.getDBVersionByApiName("getAppHomePageContentTofeeV2"),
                AllChannelRequest(
                    subcategoryId,
                    preference.customerId,
                    preference.password
                )
            )
        }
        val dbList = mutableListOf<TVChannelItem>()
        val upTime = System.currentTimeMillis()
        response.response.channelCategoryList.forEachIndexed { index, channelCategory ->
            channelCategory.channels?.forEach { channelInfo->
                dbList.add(TVChannelItem(
                    channelInfo.id.toLong(),
                    channelInfo.type ?: "LIVE",
                    index + 1,
                    channelCategory.categoryName,
                    Gson().toJson(channelInfo),
                    channelInfo.view_count?.toLong() ?: 0L
                ).apply {
                    updateTime = upTime
                })
            }
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
    }
}