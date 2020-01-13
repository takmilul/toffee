package com.banglalink.toffee.ui.verify

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn
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

    private val signingByPhone by unsafeLazy {
        SigninByPhone(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun verifyCode(
        code: String,
        regSessionToken: String,
        referralCode: String
    ): LiveData<Resource<CustomerInfoSignIn>> {
        return resultLiveData {
            val response = verifyCode.execute(code, regSessionToken, referralCode)
            getProfile.execute()//we are fetching profile after successful verification
            response
        }
    }

    fun resendCode(phoneNumber: String, referralCode: String): LiveData<Resource<String>> {
        return resultLiveData {
            signingByPhone.execute(phoneNumber, referralCode)
        }
    }
}