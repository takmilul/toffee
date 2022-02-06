package com.banglalink.toffee.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetContentCategories
import com.banglalink.toffee.apiservice.UpdateProfile
import com.banglalink.toffee.apiservice.UploadProfileImage
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfile: UpdateProfile,
    private val categoryApi: GetContentCategories,
    @ApplicationContext private val context: Context,
    private val uploadProfileImage: UploadProfileImage,
) : ViewModel() {

    val categories = MutableLiveData<List<Category>>()
    val editProfileLiveData = MutableLiveData<Resource<ProfileResponseBean>>()

    init {
        viewModelScope.launch {
            categories.value = try {
                categoryApi.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }

        }
    }

    
    fun updateProfile(editProfileForm: EditProfileForm) {
        viewModelScope.launch { 
            val response = resultFromResponse  {
                updateProfile.execute(editProfileForm)
            }
            editProfileLiveData.postValue(response)
        }
    }

    fun uploadProfileImage(photoData: Uri): LiveData<Resource<SubscriberPhotoBean>> {
        return resultLiveData {
            uploadProfileImage.execute(photoData, context)
        }
    }
}