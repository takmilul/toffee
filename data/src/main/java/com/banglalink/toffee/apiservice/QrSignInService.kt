package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PairWithTvRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class QrSignInService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(code: String): Int {

        val response = tryIO {

            toffeeApi.pairWithTv(
                PairWithTvRequest(
                    preference.customerId,
                    preference.password,
                    code
                )
            )
        }
        
        return response.response?.status ?: -1
    }
}