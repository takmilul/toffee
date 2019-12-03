package com.banglalink.toffee.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.util.unsafeLazy

class SigninByPhoneViewModel(application: Application) : BaseViewModel(application) {

    private val signinByPhone by unsafeLazy {
        SigninByPhone(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun siginIn(phoneNumber: String, referralCode: String):LiveData<Resource<Unit>> {
        return resultLiveData{
            signinByPhone.execute(phoneNumber,referralCode)
        }
    }
}