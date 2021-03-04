package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ReferrerPolicyRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ReferrerPolicyBean
import javax.inject.Inject

class GetReferrerPolicy @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(): ReferrerPolicyBean {
        val response = tryIO2 {
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