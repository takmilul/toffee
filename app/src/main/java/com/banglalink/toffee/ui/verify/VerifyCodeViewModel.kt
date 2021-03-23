package com.banglalink.toffee.ui.verify

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.SignInByPhone
import com.banglalink.toffee.apiservice.VerifyCode
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyCodeViewModel @Inject constructor(
    private val verifyCode: VerifyCode,
    private val signInByPhone: SignInByPhone,
) : BaseViewModel() {
    val verifyResponse = SingleLiveEvent<Resource<CustomerInfoSignIn>>()
    val resendCodeResponse = SingleLiveEvent<Resource<String>>()

    fun verifyCode(code: String, regSessionToken: String, referralCode: String){
        viewModelScope.launch {
            val response = resultFromResponse {verifyCode.execute(code, regSessionToken, referralCode)}
             verifyResponse.value= response
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String){
        viewModelScope.launch {
            val response = resultFromResponse {  signInByPhone.execute(phoneNumber, referralCode) }
            resendCodeResponse.value=response
        }
    }
}