package com.banglalink.toffee.ui.profile

import android.app.Application
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
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class EditProfileViewModel(application: Application) : BaseViewModel(application) {
    private val profileMutableLiveData = MutableLiveData<Resource<EditProfileForm>>()
    val profileLiveData = profileMutableLiveData.toLiveData()

    private val updateProfileMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val updateProfileLiveData = updateProfileMutableLiveData.toLiveData()

    private val updateProfile by lazy {
        UpdateProfile(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val getProfile by lazy {
        GetProfile(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val response = getProfile.execute()
                profileMutableLiveData.setSuccess(response.profile.toProfileForm())

            } catch (e: Exception) {
                profileMutableLiveData.setError(getError(e))
            }
        }
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


}