package com.banglalink.toffee.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.CheckReferralCodeStatus
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.util.unsafeLazy

class SignInViewModel : BaseViewModel() {

    private val signingByPhone by unsafeLazy {
        SigninByPhone(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val checkReferralCodeStatus by unsafeLazy {
        CheckReferralCodeStatus(RetrofitApiClient.toffeeApi)
    }

    fun signIn(phoneNumber: String, referralCode: String):LiveData<Resource<String>> {
        return resultLiveData{
            if(referralCode.isNotBlank()){
                checkReferralCodeStatus.execute(phoneNumber,referralCode)
            }
            signingByPhone.execute(phoneNumber,referralCode)
        }
    }
}