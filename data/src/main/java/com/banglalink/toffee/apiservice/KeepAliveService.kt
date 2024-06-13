package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.KeepAliveRequest
import com.banglalink.toffee.data.network.request.UpdateProfileRequest
import com.banglalink.toffee.data.network.response.KeepAliveBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.ProfileResponseBean
import javax.inject.Inject

class KeepAliveService @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
) {
    suspend fun execute(keepAliveRequest: KeepAliveRequest): KeepAliveBean? {
        val response = tryIO {
            toffeeApi.sendKeepAlive(
                keepAliveRequest
            )
        }
        return response.response
    }
}