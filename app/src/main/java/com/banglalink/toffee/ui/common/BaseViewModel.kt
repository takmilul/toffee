package com.banglalink.toffee.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.usecase.UpdateFavorite
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

open class BaseViewModel(application: Application):AndroidViewModel(application) {

    //LiveData for update favorite.
    private val favoriteMutableLiveData = SingleLiveEvent<Resource<ChannelInfo>>()
    val favoriteLiveData = favoriteMutableLiveData.toLiveData()

    private val updateFavorite by lazy {
        UpdateFavorite(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun updateFavorite(channelInfo: ChannelInfo){
        viewModelScope.launch {
            try{
                val favorite= channelInfo.favorite == null || channelInfo.favorite == "0"
                updateFavorite.execute(channelInfo,favorite)
                favoriteMutableLiveData.setSuccess(channelInfo)
            }catch (e: java.lang.Exception){
                favoriteMutableLiveData.setError(getError(e))
            }
        }
    }
}