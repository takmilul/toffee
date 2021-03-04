package com.banglalink.toffee.ui.subscription

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.SubscribePackage
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource

class SubscribePackageViewModel @ViewModelInject constructor(
    private val subscribePackage: SubscribePackage
) : ViewModel() {

    fun subscribePackage(mPackage: Package, autoRenew: Boolean = false): LiveData<Resource<String>> {
        return resultLiveData {
            subscribePackage.execute(mPackage, autoRenew)
        }
    }
}