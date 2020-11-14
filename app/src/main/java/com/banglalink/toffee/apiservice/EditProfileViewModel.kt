package com.banglalink.toffee.apiservice

import android.content.Context
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubscriberPhotoBean
import com.banglalink.toffee.ui.profile.EditProfileForm
import dagger.hilt.android.qualifiers.ApplicationContext

class EditProfileViewModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context,
    private val updateProfile: UpdateProfile,
    private val uploadProfileImage: UploadProfileImage
) : ViewModel() {
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
            uploadProfileImage.execute(photoData, context)
        }
    }


}