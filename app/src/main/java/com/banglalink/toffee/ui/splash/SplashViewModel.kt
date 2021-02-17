package com.banglalink.toffee.ui.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.ApiLogin
import com.banglalink.toffee.usecase.CheckUpdate
import com.banglalink.toffee.util.unsafeLazy

class SplashViewModel @ViewModelInject constructor(val mPref:Preference, private val authApi: AuthApi):BaseViewModel() {

    private val checkUpdate by unsafeLazy {
        CheckUpdate(mPref,authApi)
    }
    private val apiLogin by unsafeLazy {
        ApiLogin(mPref, authApi)
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

    fun isCustomerLoggedIn()=mPref.customerId != 0
}