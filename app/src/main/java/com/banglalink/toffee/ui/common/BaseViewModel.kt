package com.banglalink.toffee.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.UpdateFavorite
import com.banglalink.toffee.util.unsafeLazy

open class BaseViewModel(/*application: Application? = null*/):ViewModel() {

    private val updateFavorite by unsafeLazy {
        UpdateFavorite(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun updateFavorite(channelInfo: ChannelInfo):LiveData<Resource<ChannelInfo>>{
        return resultLiveData {
            val favorite= channelInfo.favorite == null || channelInfo.favorite == "0"
            updateFavorite.execute(channelInfo,favorite)
        }
    }
}