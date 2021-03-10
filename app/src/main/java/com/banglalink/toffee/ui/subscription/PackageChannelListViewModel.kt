package com.banglalink.toffee.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.GetPackageChannels
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PackageChannelListViewModel @Inject constructor(
    private val packageChannels: GetPackageChannels,
) : ViewModel() {

    fun getPackageChannels(packageId: Int): LiveData<Resource<List<ChannelInfo>>> {
        return resultLiveData {
            packageChannels.execute(packageId)
        }
    }
}