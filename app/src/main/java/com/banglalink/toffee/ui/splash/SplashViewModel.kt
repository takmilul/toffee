package com.banglalink.toffee.ui.splash

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.ApiLogin
import com.banglalink.toffee.usecase.CheckUpdate
import com.banglalink.toffee.util.unsafeLazy

class SplashViewModel : BaseViewModel() {

    private val checkUpdate by unsafeLazy {
        CheckUpdate(Preference.getInstance(),RetrofitApiClient.authApi)
    }
    private val apiLogin by unsafeLazy {
        ApiLogin(Preference.getInstance(), RetrofitApiClient.authApi)
    }

    fun init(skipUpdate:Boolean = false):LiveData<Resource<CustomerInfoSignIn>>{
        return resultLiveData {
            val response  = apiLogin.execute()
            if(!skipUpdate){
                checkUpdate.execute(BuildConfig.VERSION_CODE.toString())
            }
            response
        }
    }

    fun isCustomerLoggedIn()=Preference.getInstance().customerId != 0
}