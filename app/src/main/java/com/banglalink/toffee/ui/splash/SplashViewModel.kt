package com.banglalink.toffee.ui.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.ApiLogin
import com.banglalink.toffee.usecase.CheckUpdate
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SplashViewModel @ViewModelInject constructor(
    val mPref:Preference,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val authApi: AuthApi):BaseViewModel() {

    init {
        appScope.launch {
            reportAppLaunch()
        }
    }

    private val checkUpdate by unsafeLazy {
        CheckUpdate(mPref,authApi)
    }
    private val apiLogin by unsafeLazy {
        ApiLogin(mPref, authApi)
    }

    private val reportLastPlayerSession by  lazy {
        ReportLastPlayerSession(PlayerPreference.getInstance())
    }

    private val reportAppLaunch by  lazy {
        ReportAppLaunch()
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

    private fun reportAppLaunch(){
        reportAppLaunch.execute()
    }

    fun isCustomerLoggedIn()=mPref.customerId != 0
}