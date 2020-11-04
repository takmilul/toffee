package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.GetPackageList
import com.banglalink.toffee.usecase.SetAutoRenew
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class PackageListViewModel: ViewModel() {
    private val packageListMutableLiveData = MutableLiveData<Resource<List<Package>>>()
    val packageLiveData = packageListMutableLiveData.toLiveData()

    private val getPackageList by unsafeLazy {
        GetPackageList(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    private val setAutoRenew by unsafeLazy {
        SetAutoRenew(Preference.getInstance(),RetrofitApiClient.toffeeApi)
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

    fun setAutoRenew(mPackage: Package,autoRenew:Boolean):LiveData<Resource<String>>{
        return resultLiveData {
            setAutoRenew.execute(mPackage.packageId,autoRenew)
        }
    }



}