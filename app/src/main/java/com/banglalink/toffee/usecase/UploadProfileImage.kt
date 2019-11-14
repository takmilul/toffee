package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.UploadProfileImageRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class UploadProfileImage(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(
        profilePhoto: String
    ): Boolean {
        tryIO {
            toffeeApi.uploadPhoto(
                UploadProfileImageRequest(
                    profilePhoto,
                    preference.customerId,
                    preference.password
                )
            )
        }
        return true
    }
}