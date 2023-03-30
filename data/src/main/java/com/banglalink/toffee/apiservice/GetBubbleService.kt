package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.BubbleRequest
import com.banglalink.toffee.data.network.request.SubscribedUserChannelsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.BubbleConfigRepository
import com.banglalink.toffee.data.repository.RamadanBubbleRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ComingSoonContent
import com.banglalink.toffee.model.RamadanScheduled
import com.banglalink.toffee.model.RamadanScheduledResponse
import com.banglalink.toffee.model.UserChannelInfo
import javax.inject.Inject

class GetBubbleService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val ramadanBubbleRepository: RamadanBubbleRepository
) {

    suspend fun loadData(offset: Int, limit: Int): List<RamadanScheduled> {

        val request =  BubbleRequest(
            preference.customerId,
            preference.password
        )

        val response = tryIO2 {
            toffeeApi.getRamadanScheduled(
                limit,
                offset,
                preference.getDBVersionByApiName(ApiNames.GET_RAMADAN_SCHEDULED_LIST),
                request
            )
        }

        ramadanBubbleRepository.insertAll(*response.response.ramadanScheduled.toTypedArray())
        return response.response.ramadanScheduled
    }
}