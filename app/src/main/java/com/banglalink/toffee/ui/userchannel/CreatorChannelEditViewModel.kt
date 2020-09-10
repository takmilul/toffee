package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.model.UserChannel

class CreatorChannelEditViewModel: ViewModel() {
    
    val userChannel = MutableLiveData<UserChannel>()
    
}