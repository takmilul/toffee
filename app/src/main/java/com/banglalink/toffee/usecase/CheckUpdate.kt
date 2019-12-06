package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.exception.UpdateRequiredException

class CheckUpdate(private val authApi: AuthApi) {

    suspend fun execute(appVersionCode: String) {
        val response = tryIO { authApi.checkForUpdate(CheckUpdateRequest(appVersionCode)) }
        val checkUpdateBean = response.response
        if (checkUpdateBean.updateAvailable != 0) {
            throw UpdateRequiredException(
                checkUpdateBean.messageTitle,
                checkUpdateBean.message,
                checkUpdateBean.updateAvailable == 2
            )
        }
    }
}