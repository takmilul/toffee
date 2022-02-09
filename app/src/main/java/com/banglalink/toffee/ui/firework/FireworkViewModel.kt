package com.banglalink.toffee.ui.firework

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetFireWorkApiService
import com.banglalink.toffee.data.network.response.FireworkResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TermsAndCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FireworkViewModel @Inject constructor(
    private val fireWorkApiService: GetFireWorkApiService
) :ViewModel() {

    val fireworkResults = MutableLiveData<Resource<FireworkResponse>>()

    fun getFireworks(){
        viewModelScope.launch {
           val response= resultFromResponse { fireWorkApiService.execute() }
            fireworkResults.postValue(response)
        }
    }
}