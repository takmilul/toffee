package com.banglalink.toffee.ui.splash

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Customer
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.ApiLogin
import com.banglalink.toffee.usecase.CheckUpdate
import com.banglalink.toffee.usecase.GetProfile
import com.banglalink.toffee.util.unsafeLazy

class SplashViewModel(application: Application) : BaseViewModel(application) {

    private val checkUpdate by unsafeLazy {
        CheckUpdate(RetrofitApiClient.authApi)
    }
    private val apiLogin by unsafeLazy {
        ApiLogin(Preference.getInstance(), RetrofitApiClient.authApi)
    }

    private val getProfile by unsafeLazy {
        GetProfile(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun init(skipUpdate:Boolean = false):LiveData<Resource<Customer>>{
        return resultLiveData {
            if(!skipUpdate){
                checkUpdate.execute(BuildConfig.VERSION_CODE.toString())
            }
            apiLogin.execute()
            getProfile.execute()
        }
    }

    fun isCustomerLoggedIn()=Preference.getInstance().customerId != 0
}