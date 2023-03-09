package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UpdateProfileRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.ProfileResponseBean
import javax.inject.Inject

class UpdateProfile @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
) {

    suspend fun execute(editProfileForm: EditProfileForm): ProfileResponseBean {
        val response = tryIO {
            toffeeApi.updateProfile(
                UpdateProfileRequest(
                    editProfileForm.fullName,
                    editProfileForm.email,
                    editProfileForm.phoneNo,
                    editProfileForm.address,
                    preference.customerId,
                    preference.password
                )
            )
        }
        return response.response
    }
}