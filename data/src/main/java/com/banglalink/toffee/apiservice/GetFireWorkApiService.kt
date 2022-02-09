package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.FireworkRequest
import com.banglalink.toffee.data.network.response.FireworkResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class GetFireWorkApiService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
) {

    suspend fun execute(): FireworkResponse {
        val response = tryIO2 {
            toffeeApi.getFireworks(preference.getDBVersionByApiName(ApiNames.GET_FIREWORK_LIST),
                FireworkRequest(preference.customerId,preference.password)
            )
        }
        return response
    }
}