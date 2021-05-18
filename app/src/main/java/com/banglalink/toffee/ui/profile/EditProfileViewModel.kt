package com.banglalink.toffee.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.UpdateProfile
import com.banglalink.toffee.apiservice.UploadProfileImage
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubscriberPhotoBean
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfile: UpdateProfile,
    @ApplicationContext private val context: Context,
    private val uploadProfileImage: UploadProfileImage,
) : ViewModel() {
    
    fun updateProfile(editProfileForm: EditProfileForm): LiveData<Resource<Boolean>> {
        return resultLiveData {
            updateProfile.execute(
                editProfileForm.fullName,
                editProfileForm.email,
                editProfileForm.address,
                editProfileForm.phoneNo
            )
        }
    }

    fun uploadProfileImage(photoData: Uri): LiveData<Resource<SubscriberPhotoBean>> {
        return resultLiveData {
            uploadProfileImage.execute(photoData, context)
        }
    }
}