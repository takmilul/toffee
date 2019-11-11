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
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class ViewProfileViewModel(application: Application) :BaseViewModel(application){
    private val profileMutableLiveData = MutableLiveData<Resource<Profile>>()
    val profileLiveData = profileMutableLiveData.toLiveData()

    private val getProfile by lazy { 
        GetProfile(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init{
        loadCustomerProfile()
    }
    private fun loadCustomerProfile(){
        viewModelScope.launch {
            try{
                val response = getProfile.execute();
                profileMutableLiveData.setSuccess(response.profile)
            }catch (e:Exception){
                profileMutableLiveData.setError(getError(e))
            }
        }
    }

}