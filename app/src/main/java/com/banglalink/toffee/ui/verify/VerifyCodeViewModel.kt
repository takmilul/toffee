package com.banglalink.toffee.ui.verify

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.apiservice.SignInByPhone
import com.banglalink.toffee.apiservice.VerifyCode
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel

class VerifyCodeViewModel @ViewModelInject constructor(
    private val verifyCode: VerifyCode,
    private val signInByPhone: SignInByPhone
) : BaseViewModel() {

    fun verifyCode(code: String, regSessionToken: String, referralCode: String): LiveData<Resource<CustomerInfoSignIn>> {
        return resultLiveData {
            val response = verifyCode.execute(code, regSessionToken, referralCode)
            response
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String): LiveData<Resource<String>> {
        return resultLiveData {
            signInByPhone.execute(phoneNumber, referralCode)
        }
    }
}