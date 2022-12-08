package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.AccountDeleteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.AccountDeleteBean
import javax.inject.Inject

class AccountDeleteService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
) {

    suspend fun execute(): AccountDeleteBean {
        val response = tryIO2 {
            toffeeApi.accountDelete(
                AccountDeleteRequest(
                    preference.phoneNumber,
                    preference.customerId,
                    preference.password
                )
            )
        }

        return response.response
    }
}