package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.UpdateProfileRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference

class UpdateProfile(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(
        fullName: String,
        email: String,
        address: String,
        phoneNo: String
    ): Boolean {
        tryIO {
            toffeeApi.updateProfile(
                UpdateProfileRequest(
                    fullName,
                    email,
                    phoneNo,
                    address,
                    preference.customerId,
                    preference.password
                )
            )
        }
        Preference.getInstance().customerName=fullName
        return true
    }
}