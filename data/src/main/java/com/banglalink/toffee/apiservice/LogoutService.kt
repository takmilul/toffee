package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.LogoutRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.LogoutBean
import javax.inject.Inject

class LogoutService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
) {

    suspend fun execute(): LogoutBean {
        val response = tryIO2 {
            toffeeApi.unVerifyUser(
                LogoutRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response
    }
}