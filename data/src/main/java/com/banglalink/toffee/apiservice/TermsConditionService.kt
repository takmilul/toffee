package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.TermsConditionRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.TermsAndCondition
import javax.inject.Inject

class TermsConditionService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(): TermsAndCondition {
        val response = tryIO2 {
            toffeeApi.getVideoTermsAndCondition(preference.getDBVersionByApiName(ApiNames.GET_UGC_TERMS_AND_CONDITIONS),
                TermsConditionRequest(preference.customerId,preference.password)
            )
        }
        return response.response
    }
}