package com.banglalink.toffee.ui.search

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
import com.banglalink.toffee.usecase.SearchContent
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class SearchViewModel(application: Application):BaseViewModel(application) {
    private val searchResultMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val searchResultLiveData= searchResultMutableLiveData.toLiveData()

    private val searchContent by lazy {
        SearchContent(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun searchContent(searchKey:String,offset:Int){
        viewModelScope.launch {
            try{
                val response = searchContent.execute(searchKey,offset)
                searchResultMutableLiveData.setSuccess(response)

            }catch (e:Exception){
                searchResultMutableLiveData.setError(getError(e))
            }
        }
    }
}
