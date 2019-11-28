package com.banglalink.toffee.ui.splash

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.exception.UpdateRequiredException
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.ApiLogin
import com.banglalink.toffee.usecase.CheckUpdate
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : BaseViewModel(application) {

    private val splashMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val splashLiveData = splashMutableLiveData.toLiveData()

    private val checkUpdate by unsafeLazy {
        CheckUpdate(RetrofitApiClient.authApi)
    }
    private val apiLogin by unsafeLazy {
        ApiLogin(Preference.getInstance(), RetrofitApiClient.authApi)
    }

    private val getProfile by unsafeLazy {
        GetProfile(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun init(skipUpdate:Boolean = false){
        if(Preference.getInstance().customerId == 0){
            throw CustomerNotFoundException("Customer not found")
        }

        viewModelScope.launch {
            try{
                if(!skipUpdate){
                    checkUpdate.execute(BuildConfig.VERSION_CODE.toString())
                }
                apiLogin.execute()//auto login
                getProfile.execute()//fetch profile
                splashMutableLiveData.setSuccess(true)
            }
            catch (e:Exception){
                when (e) {
                    is UpdateRequiredException -> throw e
                    else -> splashMutableLiveData.setError(getError(e))
                }
            }

        }
    }
}