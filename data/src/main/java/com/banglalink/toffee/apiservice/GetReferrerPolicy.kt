package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.ReferrerPolicyRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ReferrerPolicyBean
import javax.inject.Inject

class GetReferrerPolicy @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

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