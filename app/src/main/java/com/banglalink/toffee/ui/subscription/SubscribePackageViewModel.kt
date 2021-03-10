package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.SubscribePackage
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscribePackageViewModel @Inject constructor(
    private val subscribePackage: SubscribePackage,
) : ViewModel() {

    fun subscribePackage(mPackage: Package, autoRenew: Boolean = false): LiveData<Resource<String>> {
        return resultLiveData {
            subscribePackage.execute(mPackage, autoRenew)
        }
    }
}