package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyChannelReloadViewModel: ViewModel() {

    val reloadPlaylist = MutableLiveData<Boolean>()
    val reloadVideos = MutableLiveData<Boolean>()

}