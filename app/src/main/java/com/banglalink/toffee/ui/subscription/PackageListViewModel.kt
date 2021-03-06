package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetPackageList
import com.banglalink.toffee.apiservice.SetAutoRenew
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.getError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PackageListViewModel @Inject constructor(
    private val packageList: GetPackageList,
    private val setAutoRenew: SetAutoRenew,
) : ViewModel() {

    private val packageListMutableLiveData = MutableLiveData<Resource<List<Package>>>()
    val packageLiveData = packageListMutableLiveData.toLiveData()

    init {
        viewModelScope.launch {
            getPackageList()
        }
    }

    private fun getPackageList() {
        viewModelScope.launch {
            try {
                packageListMutableLiveData.setSuccess(packageList.execute())
            } catch (e: Exception) {
                packageListMutableLiveData.setError(getError(e))
            }
        }
    }

    fun setAutoRenew(mPackage: Package, autoRenew: Boolean): LiveData<Resource<String>> {
        return resultLiveData {
            setAutoRenew.execute(mPackage.packageId, autoRenew)
        }
    }
}