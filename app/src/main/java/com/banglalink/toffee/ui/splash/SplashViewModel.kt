package com.banglalink.toffee.ui.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.ApiLogin
import com.banglalink.toffee.apiservice.CheckUpdate
import com.banglalink.toffee.apiservice.ReportAppLaunch
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SplashViewModel @ViewModelInject constructor(
    val mPref:Preference,
    private val apiLogin: ApiLogin,
    private val checkUpdate: CheckUpdate,
    @AppCoroutineScope private val appScope: CoroutineScope,
):BaseViewModel() {

    init {
        appScope.launch {
            reportAppLaunch()
        }
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

    fun reportAppLaunch(){
        appScope.launch {
            try {
                reportAppLaunch.execute()
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            }
        }
    }

    fun isCustomerLoggedIn()=mPref.customerId != 0
}