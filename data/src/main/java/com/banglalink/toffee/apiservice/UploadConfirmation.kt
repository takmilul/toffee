package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UploadConfirmationRequest
import com.banglalink.toffee.data.network.response.ResponseBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class UploadConfirmation @Inject constructor(
    private val mPref: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    suspend operator fun invoke(
        contentId: Long,
        isConfirm: Boolean,
        isCopyrightConfirm: Boolean
    ): ResponseBean? {
        val response = tryIO {
            toffeeApi.uploadConfirmation(
                UploadConfirmationRequest(
                    mPref.customerId,
                    mPref.password,
                    contentId,
                    isConfirm.toString(),
                    if(isCopyrightConfirm) 1 else 0
                )
            )
        }
        return response.response
    }
}