package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MnpStatusRequest
import com.banglalink.toffee.data.network.response.MnpStatusBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class MnpStatusService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {
    
    suspend fun execute(): MnpStatusBean? {
        val response = tryIO {
            toffeeApi.getMnpStatus(
                MnpStatusRequest(
                    customerId = preference.customerId,
                    password = preference.password,
                    telcoId = 1
                )
            )
        }
        return response.response
    }
}