package com.banglalink.toffee.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.LoginByPhone
import com.banglalink.toffee.apiservice.VerifyCodeService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.SendLoginLogEvent
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyCodeViewModel @Inject constructor(
    private val verifyCodeService: VerifyCodeService,
    private val loginByPhone: LoginByPhone,
    private val sendLoginLogEvent: SendLoginLogEvent,
) : ViewModel() {
    
    val verifyResponse = SingleLiveEvent<Resource<CustomerInfoLogin>>()
    val resendCodeResponse = SingleLiveEvent<Resource<String?>>()

    fun verifyCode(code: String, regSessionToken: String, referralCode: String){
        viewModelScope.launch {
            val response = resultFromResponse { verifyCodeService.execute(code, regSessionToken, referralCode) }
            verifyResponse.value= response
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String){
        viewModelScope.launch {
            val response = resultFromResponse {  loginByPhone.execute(phoneNumber, referralCode) }
            resendCodeResponse.value = response
        }
    }
    
    fun sendLoginLogData(apiName: String) {
        viewModelScope.launch {
            sendLoginLogEvent.execute(apiName)
        }
    }
}