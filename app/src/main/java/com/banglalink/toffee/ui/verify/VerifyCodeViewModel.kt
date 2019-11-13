package com.banglalink.toffee.ui.verify

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.usecase.VerifyCode
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class VerifyCodeViewModel(application: Application) : BaseViewModel(application) {

    private val verifyCodeMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val verifyCodeLiveData = verifyCodeMutableLiveData.toLiveData()

    private val getProfile: GetProfile by lazy {
        GetProfile(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val verifyCode by lazy {
        VerifyCode(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val signinByPhone by lazy {
        SigninByPhone(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            try {
                verifyCode.execute(code)
                getProfile.execute()
                verifyCodeMutableLiveData.setSuccess(true)
            } catch (e: Exception) {
                verifyCodeMutableLiveData.setError(getError(e))
            }

        }
    }

    fun resendCode(phoneNumber: String, referralCode: String) {
        viewModelScope.launch {
            try {
                signinByPhone.execute(phoneNumber, referralCode)
            } catch (e: Exception) {
                verifyCodeMutableLiveData.setError(getError(e))
            }
        }
    }
}