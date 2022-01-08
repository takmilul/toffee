package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.OffenseRequest
import com.banglalink.toffee.data.network.request.StingrayConetntRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.OffenseType
import javax.inject.Inject

class GetStingrayContentService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
): BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getStingrayContents(
                "stingray",
                0,
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_STINGRAY_CONTENTS),
                StingrayConetntRequest(
                    preference.customerId,
                    preference.password,
                    1,
                    "stingray"

                )
            )
        }

        return response.response.channels ?: emptyList()
    }
}