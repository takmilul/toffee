package com.banglalink.toffee.ui.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.apiservice.GetProfile

class ViewProfileViewModel @ViewModelInject constructor(
    private val profileApi: GetProfile
) :BaseViewModel(){
    fun loadCustomerProfile():LiveData<Resource<EditProfileForm>>{
        return resultLiveData {
            profileApi().profile.toProfileForm()
        }
    }
}