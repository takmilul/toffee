package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.OffenseRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.OffenseType
import javax.inject.Inject

class GetOffenseService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
): BaseApiService<OffenseType> {

    override suspend fun loadData(offset: Int, limit: Int): List<OffenseType> {
        val response = tryIO {
            toffeeApi.getOffenseList(
                limit, offset,
                preference.getDBVersionByApiName(ApiNames.GET_OFFENCE_LIST),
                OffenseRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response.offenseTypeList ?: emptyList()
    }
}