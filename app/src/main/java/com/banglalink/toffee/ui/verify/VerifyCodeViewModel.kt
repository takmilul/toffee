package com.banglalink.toffee.ui.verify

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.usecase.VerifyCode
import com.banglalink.toffee.util.unsafeLazy

class VerifyCodeViewModel @ViewModelInject constructor(val mPref:Preference):BaseViewModel() {

    private val verifyCode by unsafeLazy {
        VerifyCode(mPref, RetrofitApiClient.toffeeApi)
    }

    private val signingByPhone by unsafeLazy {
        SigninByPhone(mPref, RetrofitApiClient.toffeeApi)
    }

    fun verifyCode(
        code: String,
        regSessionToken: String,
        referralCode: String
    ): LiveData<Resource<CustomerInfoSignIn>> {
        return resultLiveData {
            val response = verifyCode.execute(code, regSessionToken, referralCode)
            response
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String): LiveData<Resource<String>> {
        return resultLiveData {
            signingByPhone.execute(phoneNumber, referralCode)
        }
    }
}