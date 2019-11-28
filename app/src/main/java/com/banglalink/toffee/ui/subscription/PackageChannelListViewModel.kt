package com.banglalink.toffee.ui.subscription

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.GetPackageChannels
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class PackageChannelListViewModel(application: Application):BaseViewModel(application){
    private val channelListMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val channelListLiveData = channelListMutableLiveData.toLiveData()

    private val getPackageChannelList by unsafeLazy {
        GetPackageChannels(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun getPackageChannels(packageId:Int){
        viewModelScope.launch {
            try{
                val response = getPackageChannelList.execute(packageId)
                channelListMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                channelListMutableLiveData.setError(getError(e))
            }
        }
    }
}