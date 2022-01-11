package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.StingrayContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import javax.inject.Inject

class GetStingrayContentService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
    private val tvChannelRepo: TVChannelRepository
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getStingrayContents(
                "stingray",
                0,
                0,
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_STINGRAY_CONTENTS),
                StingrayContentRequest(
                    preference.customerId,
                    preference.password,
                    1,
                    "stingray"
                )
            )
        }
        val dbList = mutableListOf<TVChannelItem>()
        val upTime = System.currentTimeMillis()
        response.response.channels?.filter {
            try {
                Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
            } catch (e: Exception) {
                true
            }
        }?.forEach {
            dbList.add(
                TVChannelItem(
                    it.id.toLong(),
                    it.type ?: "LIVE",
                    1,
                    "Karaoke - Stingray",
                    Gson().toJson(it),
                    it.view_count?.toLong() ?: 0L,
                    it.isStingray
                ).apply {
                    updateTime = upTime
                })
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
        return response.response.channels ?: emptyList()
    }
}