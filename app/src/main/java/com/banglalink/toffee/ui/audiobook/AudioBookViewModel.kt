package com.banglalink.toffee.ui.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.AudioBookSeeMoreService
import com.banglalink.toffee.apiservice.KabbikHomeApiService
import com.banglalink.toffee.apiservice.KabbikLoginApiService
import com.banglalink.toffee.data.network.response.KabbikHomeApiResponse
import com.banglalink.toffee.data.network.response.KabbikLoginApiResponse
import com.banglalink.toffee.data.network.response.AudioBookSeeMoreResponse
import com.banglalink.toffee.data.network.util.resultFromExternalResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioBookViewModel @Inject constructor(
    private val apiService: KabbikLoginApiService,
    private val homeApiService: KabbikHomeApiService,
    private val audioBookSeeMoreService: AudioBookSeeMoreService,

    ) : ViewModel() {
    val loginResponse = SingleLiveEvent<Resource<KabbikLoginApiResponse>>()
    val homeApiResponse = SingleLiveEvent<Resource<KabbikHomeApiResponse>>()
    val audioBookSeeMoreResponse = SingleLiveEvent<Resource<AudioBookSeeMoreResponse?>>()

    fun login() {
        viewModelScope.launch {
            val response= resultFromExternalResponse {  apiService.execute() }
            loginResponse.value=response
        }
    }

    fun homeApi(token: String){
        viewModelScope.launch {
            val response= resultFromExternalResponse {  homeApiService.execute(token) }
            homeApiResponse.value = response
        }
    }

    fun getAudioBookSeeMore(myTitle: String) {
        viewModelScope.launch {
            val response = resultFromResponse { audioBookSeeMoreService.execute(myTitle) }
            audioBookSeeMoreResponse.value = response
        }
    }
}