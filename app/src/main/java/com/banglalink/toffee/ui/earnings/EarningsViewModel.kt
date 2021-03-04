package com.banglalink.toffee.ui.earnings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetEarningInfo
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Earning
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class EarningsViewModel : ViewModel() {
    private val earningRepo by unsafeLazy { GetEarningInfo() }
    var data: Earning? = null
    private val _liveData = MutableLiveData<Resource<Earning>>()
    val liveData = _liveData.toLiveData()

    fun getEarningInfo() {
        viewModelScope.launch {
            val response = resultFromResponse { earningRepo.execute() }
            _liveData.postValue(response)
            
            if (response is Success) {
                data = response.data
            }
        }
    }
}