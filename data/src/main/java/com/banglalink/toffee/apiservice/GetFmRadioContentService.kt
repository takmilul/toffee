package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.FmRadioContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import javax.inject.Inject

class GetFmRadioContentService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    private val tvChannelRepo: TVChannelRepository
): BaseApiService<ChannelInfo> {

    val gson = Gson()
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getFmRadioContents(
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_FM_RADIO_CONTENTS),
                FmRadioContentRequest(
                    preference.customerId,
                    preference.password,
                    1
                )
            )
        }

        //Saving Radio Banner Img
        preference.radioBannerImgUrl.value=response.response.radio_banner.toString()

        val dbList = mutableListOf<TVChannelItem>()
        val upTime = System.currentTimeMillis()
        response.response.channels?.filter {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            localSync.syncData(it, isFromCache = response.isFromCache)
            
            if (!it.isExpired) {
                dbList.add(
                    TVChannelItem(
                        it.id.toLong(),
                        "RADIO",
                        1,
                        "Fm Radio Playlist",
                        gson.toJson(it),
                        it.view_count?.toLong() ?: 0L,
                        it.isStingray,
                        it.isFmRadio
                    ).apply {
                        updateTime = upTime
                    }
                )
            }
            !it.isExpired
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
        return response.response.channels ?: emptyList()
    }
}