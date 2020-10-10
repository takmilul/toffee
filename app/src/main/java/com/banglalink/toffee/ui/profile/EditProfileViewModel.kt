package com.banglalink.toffee.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubscriberPhotoBean
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.usecase.UpdateProfile
import com.banglalink.toffee.usecase.UploadProfileImage
import com.banglalink.toffee.util.unsafeLazy

class EditProfileViewModel(private val application: Application) : BaseViewModel(application) {

    private val updateProfile by unsafeLazy {
        UpdateProfile(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val uploadProfileImage by unsafeLazy {
        UploadProfileImage(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }


    fun updateProfile(editProfileForm: EditProfileForm):LiveData<Resource<Boolean>> {

        return resultLiveData{updateProfile.execute(
            editProfileForm.fullName,
            editProfileForm.email,
            editProfileForm.address,
            editProfileForm.phoneNo
        )}
    }

    fun uploadProfileImage(photoData: Uri):LiveData<Resource<SubscriberPhotoBean>> {
        return resultLiveData{
            uploadProfileImage.execute(photoData, application)
        }
    }


}