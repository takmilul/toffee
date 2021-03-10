package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import com.banglalink.toffee.util.SingleLiveEvent

class MyChannelReloadViewModel : ViewModel() {
    val reloadPlaylist = SingleLiveEvent<Boolean>()
    val reloadVideos = SingleLiveEvent<Boolean>()
}