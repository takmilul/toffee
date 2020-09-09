package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannel
import com.banglalink.toffee.usecase.EditChannel
import com.banglalink.toffee.util.unsafeLazy

class CreatorChannelEditViewModel: ViewModel() {
    
    val userChannel = MutableLiveData<UserChannel>()
    private val editChannel by unsafeLazy { EditChannel(Preference.getInstance(), RetrofitApiClient.toffeeApi) }
    
    fun getEditInfo(): LiveData<Resource<UserChannel>>{
        return resultLiveData { 
            editChannel.execute()
        }
    }
    
}