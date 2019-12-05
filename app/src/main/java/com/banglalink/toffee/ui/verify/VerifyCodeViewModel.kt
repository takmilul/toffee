package com.banglalink.toffee.ui.verify

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Customer
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.usecase.SigninByPhone
import com.banglalink.toffee.usecase.VerifyCode
import com.banglalink.toffee.util.unsafeLazy

class VerifyCodeViewModel(application: Application) : BaseViewModel(application) {

    private val getProfile: GetProfile by unsafeLazy {
        GetProfile(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val verifyCode by unsafeLazy {
        VerifyCode(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    private val signinByPhone by unsafeLazy {
        SigninByPhone(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun verifyCode(code: String) :LiveData<Resource<Customer>>{
        return resultLiveData {
            verifyCode.execute(code)
            getProfile.execute()
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String):LiveData<Resource<Unit>> {
        return resultLiveData {
            signinByPhone.execute(phoneNumber,referralCode)
        }
    }
}