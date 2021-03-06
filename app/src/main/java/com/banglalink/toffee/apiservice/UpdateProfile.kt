package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UpdateProfileRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import javax.inject.Inject

class UpdateProfile @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
) {

    suspend fun execute(fullName: String, email: String, address: String, phoneNo: String, ): Boolean {
        tryIO2 {
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
        preference.customerName = fullName
        return true
    }
}