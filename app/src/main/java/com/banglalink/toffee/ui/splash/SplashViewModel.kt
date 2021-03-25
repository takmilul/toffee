package com.banglalink.toffee.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.ApiLogin
import com.banglalink.toffee.apiservice.CheckUpdate
import com.banglalink.toffee.apiservice.ReportAppLaunch
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val mPref: SessionPreference,
    private val apiLogin: ApiLogin,
    private val checkUpdate: CheckUpdate,
    @AppCoroutineScope private val appScope: CoroutineScope,
) : BaseViewModel() {

    val apiLoginResponse = SingleLiveEvent<Resource<Any>>()
    init {
        appScope.launch {
            reportAppLaunch()
        }
    }

    private val reportAppLaunch by lazy {
        ReportAppLaunch()
    }

    fun loginResponse(skipUpdate: Boolean = false) {
        viewModelScope.launch {
            val response = resultFromResponse { apiLogin.execute() }
            if (!skipUpdate) {
              val updateResponse = resultFromResponse { checkUpdate.execute(BuildConfig.VERSION_CODE.toString())}
              if(updateResponse is Resource.Failure) apiLoginResponse.value = updateResponse
            }
            apiLoginResponse.value=response
        }
    }

    fun reportAppLaunch() {
        appScope.launch {
            try {
                reportAppLaunch.execute()
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("Exception in ReportAppLaunch")
            }
        }
    }

    fun isCustomerLoggedIn() = mPref.customerId != 0
}