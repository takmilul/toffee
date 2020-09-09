package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannel
import com.banglalink.toffee.usecase.EditChannel
import com.banglalink.toffee.util.unsafeLazy

class CreatorChannelEditViewModel: ViewModel() {
    private val _userChannel = MutableLiveData<UserChannel>()
    private val _selectedCategoryPosition: MutableLiveData<Int> = MutableLiveData()
    private val _selectedSubscriptionPricePosition: MutableLiveData<Int> = MutableLiveData()
    
    val userChannel = _userChannel.toLiveData()
    val selectedCategoryPosition = _selectedCategoryPosition.toLiveData()
    val selectedSubscriptionPricePosition = _selectedSubscriptionPricePosition.toLiveData()
    
    private val editChannel by unsafeLazy { EditChannel(Preference.getInstance(), RetrofitApiClient.toffeeApi) }
    
    fun getEditInfo(): LiveData<Resource<UserChannel>>{
        return resultLiveData { 
            val data = editChannel.execute()
            _userChannel.postValue(data)
            _selectedCategoryPosition.postValue(data.categoryList.indexOf(data.selectedCategory))
            _selectedSubscriptionPricePosition.postValue(data.subscriptionPriceList.indexOf(data.selectedSubscriptionPrice))
            editChannel.execute()
        }
    }
    
    /*fun getData(){
        viewModelScope.launch { 
            _userChannel.value = resultFromResponse { editChannel.execute() }
            _selectedCategoryPosition.value
        }
    }*/
}