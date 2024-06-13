package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.exception.UpdateRequiredException
import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.DecorationConfig
import javax.inject.Inject

class CheckForUpdateService @Inject constructor(private val preference: SessionPreference, private val authApi: AuthApi) {

    suspend fun execute(appVersionCode: String): DecorationConfig? {
        val response = tryIO {
            authApi.checkForUpdateV2(
                preference.getDBVersionByApiName("checkForUpdateV2"),
                checkUpdateRequest = CheckUpdateRequest(appVersionCode)
            )
        }
        val checkUpdateBean = response.response
        if (checkUpdateBean != null && checkUpdateBean.updateAvailable != 0) {
            throw UpdateRequiredException(
                checkUpdateBean.messageTitle ?: "Update Available",
                checkUpdateBean.message ?: "An update is available. Please update to the latest version",
                checkUpdateBean.updateAvailable == 2
            )
        }
        return response.response?.decorationConfig?.get(0)?.apply { 
            isFromCache = response.isFromCache
        }
    }
}