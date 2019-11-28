package com.banglalink.toffee.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class SigninByPhoneViewModel(application: Application) : BaseViewModel(application) {

    private val signinMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val signinLiveData = signinMutableLiveData.toLiveData()

    private val signinByPhone by unsafeLazy {
        SigninByPhone(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun siginIn(phoneNumber: String, referralCode: String) {
        viewModelScope.launch {
            try {
                signinByPhone.execute(phoneNumber, referralCode)
                signinMutableLiveData.setSuccess(true)
            } catch (e: Exception) {
                e.printStackTrace()
                signinMutableLiveData.setError(getError(e))
            }

        }
    }
}