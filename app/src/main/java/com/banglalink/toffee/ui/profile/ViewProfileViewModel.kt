package com.banglalink.toffee.ui.profile

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.util.unsafeLazy

class ViewProfileViewModel :BaseViewModel(){

    private val getProfile by unsafeLazy {
        GetProfile(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun loadCustomerProfile():LiveData<Resource<EditProfileForm>>{
        return resultLiveData {
            getProfile.execute().profile.toProfileForm()
        }
    }

}