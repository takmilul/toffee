package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ReferrerPolicyRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ReferrerPolicyBean

class GetReferrerPolicy(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(): ReferrerPolicyBean {
        val response = tryIO {
            toffeeApi.getReferrerPolicy(
                ReferrerPolicyRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        return response.referrerPolicyBean
    }
}