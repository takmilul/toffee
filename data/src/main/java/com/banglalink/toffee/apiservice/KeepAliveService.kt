package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.KeepAliveRequest
import com.banglalink.toffee.data.network.response.KeepAliveBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class KeepAliveService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun execute(keepAliveRequest: KeepAliveRequest): KeepAliveBean? {
        val response = tryIO {
            toffeeApi.sendKeepAlive(
                url = preference.keepAliveApiEndPoint + "/v1/keep-alive",
                keepAliveRequest = keepAliveRequest
            )
        }
        return response.response
    }
}