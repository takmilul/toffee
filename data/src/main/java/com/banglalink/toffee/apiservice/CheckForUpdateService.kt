package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.exception.UpdateRequiredException
import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.DecorationConfig
import javax.inject.Inject

class CheckForUpdateService @Inject constructor(private val preference: SessionPreference, private val authApi: AuthApi) {

    suspend fun execute(appVersionCode: String): DecorationConfig? {
        val response = tryIO2 {
            authApi.checkForUpdateV2(
                preference.getDBVersionByApiName("checkForUpdateV2"),
                checkUpdateRequest = CheckUpdateRequest(appVersionCode)
            )
        }
        val checkUpdateBean = response.response
        if (checkUpdateBean.updateAvailable != 0) {
            throw UpdateRequiredException(
                checkUpdateBean.messageTitle,
                checkUpdateBean.message,
                checkUpdateBean.updateAvailable == 2
            )
        }
        return response.response.decorationConfig?.get(0)
    }
}