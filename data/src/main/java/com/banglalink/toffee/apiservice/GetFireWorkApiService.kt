package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.FeatureContentRequest
import com.banglalink.toffee.data.network.request.FireworkRequest
import com.banglalink.toffee.data.network.request.TermsConditionRequest
import com.banglalink.toffee.data.network.response.FireworkResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.FeatureContentBean
import com.banglalink.toffee.model.TermsAndCondition
import com.banglalink.toffee.util.Utils
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