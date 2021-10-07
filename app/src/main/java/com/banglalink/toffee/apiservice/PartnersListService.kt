package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.PartnersRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class PartnersListService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    @Assisted private val requestParams: ChannelRequestParams,
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getPartnersList(
                requestParams.type,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcPartnerList"),
                PartnersRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response.channels?.filter {
            try {
                Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
            } catch (e: Exception) {
                true
            }
        } ?: emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): PartnersListService
    }
}