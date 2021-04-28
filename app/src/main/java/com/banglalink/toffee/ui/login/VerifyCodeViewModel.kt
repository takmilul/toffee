package com.banglalink.toffee.ui.login

import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.LoginByPhone
import com.banglalink.toffee.apiservice.VerifyCode
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyCodeViewModel @Inject constructor(
    private val verifyCode: VerifyCode,
    private val loginByPhone: LoginByPhone,
) : BaseViewModel() {
    val verifyResponse = SingleLiveEvent<Resource<CustomerInfoLogin>>()
    val resendCodeResponse = SingleLiveEvent<Resource<String>>()

    fun verifyCode(code: String, regSessionToken: String, referralCode: String){
        viewModelScope.launch {
            val response = resultFromResponse {verifyCode.execute(code, regSessionToken, referralCode)}
             verifyResponse.value= response
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String){
        viewModelScope.launch {
            val response = resultFromResponse {  loginByPhone.execute(phoneNumber, referralCode) }
            resendCodeResponse.value=response
        }
    }
}