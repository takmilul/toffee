package com.banglalink.toffee.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetRelativeContents
import com.banglalink.toffee.util.unsafeLazy

class CatchupDetailsViewModel:BaseViewModel() {


    private val getRelativeContents by unsafeLazy {
        GetRelativeContents(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun getContents(channelInfo: ChannelInfo):LiveData<Resource<List<ChannelInfo>>>{
        return resultLiveData {
            getRelativeContents.execute(channelInfo)
        }
    }
}