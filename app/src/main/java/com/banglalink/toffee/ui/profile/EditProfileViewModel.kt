package com.banglalink.toffee.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Profile
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.usecase.UpdateProfile
import com.banglalink.toffee.usecase.UploadProfileImage
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class EditProfileViewModel(application: Application) : BaseViewModel(application) {
    private val updateProfileMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val updateProfileLiveData = updateProfileMutableLiveData.toLiveData()

    private val uploadPhotoMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val uploadPhotoLiveData = uploadPhotoMutableLiveData.toLiveData()


    private val updateProfile by lazy {
        UpdateProfile(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val uploadProfileImage by lazy {
        UploadProfileImage(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val getProfile by lazy {
        GetProfile(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }


    fun updateProfile(editProfileForm: EditProfileForm) {
        viewModelScope.launch {
            try {
                updateProfile.execute(
                    editProfileForm.fullName,
                    editProfileForm.email,
                    editProfileForm.address,
                    editProfileForm.phoneNo
                )
                updateProfileMutableLiveData.setSuccess(true)
            } catch (e: Exception) {
                updateProfileMutableLiveData.setError(getError(e))
            }
        }
    }

    fun uploadProfileImage(photoData: Uri) {
        viewModelScope.launch {
            try {
                uploadProfileImage.execute(photoData,getApplication())
                getProfile.execute()//we are calling get profile to update the url in preference
                uploadPhotoMutableLiveData.setSuccess(true)
            } catch (e: Exception) {
                uploadPhotoMutableLiveData.setError(getError(e))
            }
        }
    }


}