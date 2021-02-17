package com.banglalink.toffee.ui.subscription

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.SubscribePackage
import com.banglalink.toffee.util.unsafeLazy

class SubscribePackageViewModel @ViewModelInject constructor(private val toffeeApi: ToffeeApi): ViewModel() {
    private val subscribePackage by unsafeLazy {
        SubscribePackage(Preference.getInstance(), toffeeApi)
    }

    fun subscribePackage(mPackage : Package,autoRenew:Boolean = false):LiveData<Resource<String>>{
        return resultLiveData {
            subscribePackage.execute(mPackage,autoRenew)
        }
    }
}