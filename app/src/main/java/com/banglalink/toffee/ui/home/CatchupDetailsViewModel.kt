package com.banglalink.toffee.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetRelativeContents
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class CatchupDetailsViewModel(application: Application):BaseViewModel(application) {

    private val relativeContentMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val relativeContentLiveData = relativeContentMutableLiveData.toLiveData()

    private val getRelativeContents by unsafeLazy {
        GetRelativeContents(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun getContents(channelInfo: ChannelInfo){
        viewModelScope.launch {
            try{
                val response = getRelativeContents.execute(channelInfo)
                relativeContentMutableLiveData.setSuccess(response)
            }catch (e:Exception){
                relativeContentMutableLiveData.setError(getError(e))
            }
        }
    }
}