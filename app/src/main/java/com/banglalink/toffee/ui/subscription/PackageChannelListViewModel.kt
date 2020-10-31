package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.GetPackageChannels
import com.banglalink.toffee.util.unsafeLazy

class PackageChannelListViewModel: ViewModel(){

    private val getPackageChannelList by unsafeLazy {
        GetPackageChannels(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun getPackageChannels(packageId:Int):LiveData<Resource<List<ChannelInfo>>>{
        return resultLiveData {
            getPackageChannelList.execute(packageId)
        }
    }
}