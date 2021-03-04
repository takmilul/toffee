package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UploadConfirmationRequest
import com.banglalink.toffee.data.network.response.ResponseBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import javax.inject.Inject

class UploadConfirmation @Inject constructor(
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(
        contentId: Long,
        isConfirm: Boolean
    ): ResponseBean {
        val response = tryIO2 {
            toffeeApi.uploadConfirmation(
                UploadConfirmationRequest(
                    mPref.customerId,
                    mPref.password,
                    contentId,
                    isConfirm.toString()
                )
            )
        }
        return response.response
    }
}