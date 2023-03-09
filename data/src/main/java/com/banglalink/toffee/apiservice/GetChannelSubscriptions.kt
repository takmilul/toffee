package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import javax.inject.Inject

class GetChannelSubscriptions @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi)
    : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getHistoryContents(
                HistoryContentRequest(
                    preference.customerId,
                    preference.password,
                    offset,
                    limit
                )
            )
        }
        
        return response.response.channels?.map {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            it
        } ?: emptyList()
    }
}