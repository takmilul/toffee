package com.banglalink.toffee.ui.subscription

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.SubscribePackage
import com.banglalink.toffee.util.unsafeLazy

class SubscribePackageViewModel(application: Application):BaseViewModel(application) {
    private val subscribePackage by unsafeLazy {
        SubscribePackage(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun subscribePackage(mPackage : Package,autoRenew:Boolean = false):LiveData<Resource<String>>{
        return resultLiveData {
            subscribePackage.execute(mPackage,autoRenew)
        }
    }
}