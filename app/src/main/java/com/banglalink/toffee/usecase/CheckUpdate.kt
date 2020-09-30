package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.UpdateRequiredException
import com.banglalink.toffee.util.EncryptionUtil
import com.google.gson.Gson

class CheckUpdate(private val preference: Preference, private val authApi: AuthApi) {

    suspend fun execute(appVersionCode: String) {
        val response = tryIO2 { authApi.checkForUpdateV2(preference.getDBVersionByApiName("checkForUpdateV2"),CheckUpdateRequest(appVersionCode)) }
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