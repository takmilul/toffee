package com.banglalink.toffee.ui.splash

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
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

    private val customerLoginMutableLiveData = MutableLiveData<Resource<Boolean>>()
    val customerLoginLiveData = customerLoginMutableLiveData.toLiveData()

    private val updateRequiredMutableLiveData = MutableLiveData<UpdateRequiredException>()
    val updateRequiredLiveData = updateRequiredMutableLiveData.toLiveData()

    private val checkUpdate by unsafeLazy {
        CheckUpdate(RetrofitApiClient.authApi)
    }
    private val apiLogin by unsafeLazy {
        ApiLogin(Preference.getInstance(), RetrofitApiClient.authApi)
    }

    private val getProfile by unsafeLazy {
        GetProfile(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init {
        init(false)
    }

    fun init(skipUpdate:Boolean = false){

        viewModelScope.launch {
            try{
                if(!skipUpdate){
                    checkUpdate.execute(BuildConfig.VERSION_CODE.toString())
                }
                if(isCustomerLoggedIn()){
                    apiLogin.execute()//auto login
                    getProfile.execute()//fetch profile
                    customerLoginMutableLiveData.setSuccess(true)
                }else{
                    customerLoginMutableLiveData.setSuccess(false)
                }
            }
            catch (e:Exception){
                when (e) {
                    is UpdateRequiredException -> {
                        updateRequiredMutableLiveData.postValue(e)
                    }
                    else -> customerLoginMutableLiveData.setError(getError(e))
                }
            }

        }
    }

    private fun isCustomerLoggedIn()=Preference.getInstance().customerId != 0
}