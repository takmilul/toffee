package com.banglalink.toffee.ui.subscription

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetPackageList
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class PackageListViewModel(application: Application):BaseViewModel(application) {
    private val packageListMutableLiveData = MutableLiveData<Resource<List<Package>>>()
    val packageLiveData = packageListMutableLiveData.toLiveData()

    private val getPackageList by lazy {
        GetPackageList(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init {
        viewModelScope.launch {
           getPackageList()
        }

    }

    private fun getPackageList(){
        viewModelScope.launch {
            try{
                packageListMutableLiveData.setSuccess(getPackageList.execute())
            }catch (e:Exception){
                packageListMutableLiveData.setError(getError(e))
            }
        }
    }



}