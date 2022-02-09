package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import javax.inject.Inject

class GetChannelWithCategory @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val tvChannelRepo: TVChannelRepository
) {
    suspend operator fun invoke(subcategoryId: Int) {
        val response = tryIO2 {
            toffeeApi.getChannels(
                preference.getDBVersionByApiName(ApiNames.GET_ALL_TV_CHANNELS),
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
            channelCategory.channels?.filter {
                try {
                    Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
                } catch (e: Exception) {
                    true
                }
            }?.forEach { channelInfo->
                dbList.add(TVChannelItem(
                    channelInfo.id.toLong(),
                    channelInfo.type ?: "LIVE",
                    index + 1,
                    channelCategory.categoryName,
                    Gson().toJson(channelInfo),
                    channelInfo.view_count?.toLong() ?: 0L,
                    channelInfo.isStingray
                ).apply {
                    updateTime = upTime
                })
            }
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
    }
}