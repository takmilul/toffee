package com.banglalink.toffee.ui.player

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.SendViewContentEvent
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch
import java.lang.Exception

class PlayerFragmentViewModel(application: Application):BaseViewModel(application) {
    private val sendViewContentEvent by lazy {
        SendViewContentEvent(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }


    fun sendViewContentEvent(channelInfo: ChannelInfo){
        viewModelScope.launch {
            try {
                sendViewContentEvent.execute(channelInfo)
            }catch (e:Exception){
                getError(e)
            }
        }
    }
}