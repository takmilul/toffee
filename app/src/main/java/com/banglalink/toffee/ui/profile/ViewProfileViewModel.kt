package com.banglalink.toffee.ui.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.apiservice.GetProfile
import com.banglalink.toffee.apiservice.MyChannelEditDetailService
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelEditBean
import kotlinx.coroutines.launch

class ViewProfileViewModel @ViewModelInject constructor(private val myChannelDetailApiService: MyChannelEditDetailService,
                                                        private val profileApi: GetProfile
) :BaseViewModel(){
    private val _data = MutableLiveData<Resource<MyChannelEditBean>>()
    val liveData = _data.toLiveData()
    fun loadCustomerProfile():LiveData<Resource<EditProfileForm>>{
        return resultLiveData {
            profileApi().profile.toProfileForm()
        }
    }
    fun editChannel(myChannelEditRequest: MyChannelEditRequest) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { myChannelDetailApiService.execute(myChannelEditRequest) })
        }
    }
}