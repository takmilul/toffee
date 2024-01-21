package com.banglalink.toffee.ui.audiobook

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.AudioBookEpisodeListService
import com.banglalink.toffee.apiservice.AudioBookSeeMoreService
import com.banglalink.toffee.apiservice.KabbikHomeApiService
import com.banglalink.toffee.apiservice.KabbikLoginApiService
import com.banglalink.toffee.apiservice.KabbikTopBannerApiService
import com.banglalink.toffee.data.network.response.AudioBookSeeMoreResponse
import com.banglalink.toffee.data.network.response.KabbikCategory
import com.banglalink.toffee.data.network.response.KabbikHomeApiResponse
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse
import com.banglalink.toffee.data.network.util.resultFromExternalResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.util.DateComparisonResult
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.compareDates
import com.banglalink.toffee.util.currentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AudioBookViewModel @Inject constructor(
    private val mPref: SessionPreference,
    @ApplicationContext private val appContext: Context,
    private val loginApiService: KabbikLoginApiService,
    private val homeApiService: KabbikHomeApiService,
    private val topBannerApiService: KabbikTopBannerApiService,
    private val audioBookSeeMoreService: AudioBookSeeMoreService,
    private val audioBookEpisodeListService: AudioBookEpisodeListService,
) : ViewModel() {
    val homeApiResponse = SingleLiveEvent<Resource<KabbikHomeApiResponse>>()
    val topBannerApiResponse = SingleLiveEvent<Resource<KabbikTopBannerApiResponse>>()
    val topBannerApiResponseCompose = mutableStateOf(emptyList<KabbikTopBannerApiResponse.BannerItemBean>())
    val audioBookSeeMoreResponse = SingleLiveEvent<Resource<AudioBookSeeMoreResponse?>>()
    val audioBookEpisodeResponse = SingleLiveEvent<Resource<List<ChannelInfo>?>>()
    val audioBookEpisodeResponseObserver = MutableSharedFlow<Resource<List<ChannelInfo>?>>(replay = 1)
    val isLoadingCategory = mutableStateOf(false)
    val homeApiResponseCompose = mutableStateOf(emptyList<KabbikCategory>())
    
    fun grantToken(success: suspend (token: String) -> Unit, failure: () -> Unit) {
        viewModelScope.launch {
            val result = compareDates(
                fromDate = currentDateTime,
                toDate = mPref.kabbikTokenExpiryTime
            )
            when (result) {
                DateComparisonResult.EARLIER -> {
                    success.invoke(mPref.kabbikAccessToken)
                }
                
                else -> {
                    when (val loginResponse = resultFromExternalResponse { loginApiService.execute() }) {
                        is Success -> {
                            val systemDate =
                                Utils.dateToStr(Utils.getDate(Date().toString(), "yyyy-MM-dd")).toString()
                            mPref.kabbikTokenExpiryTime = systemDate + " " + loginResponse.data.expiry
                            loginResponse.data.token?.let {
                                mPref.kabbikAccessToken = it
                                success.invoke(it)
                            }
                        }
                        
                        is Failure -> {
                            appContext.showToast("Something went wrong. Please try again later.")
                            failure.invoke()
                        }
                    }
                }
            }
        }
    }
    
    fun homeApi(token: String) {
        viewModelScope.launch {
            val response = resultFromExternalResponse { homeApiService.execute(token) }
            homeApiResponse.value = response
        }
    }
    
    fun topBannerApi(token: String) {
        viewModelScope.launch {
            val response = resultFromExternalResponse { topBannerApiService.execute(token) }
            topBannerApiResponse.value = response
        }
    }
    
    fun topBannerApiCompose(token: String) {
        viewModelScope.launch {
            Log.i("kabbik_", "in banner api token: $token")
            val response = resultFromExternalResponse { topBannerApiService.execute(token) }
            when (response) {
                is Success -> {
                    topBannerApiResponseCompose.value = response.data.bannerItems
                }
                
                is Failure -> {
                }
            }
        }
    }
    
    fun homeApiCompose(token: String) {
        isLoadingCategory.value = true
        viewModelScope.launch {
            Log.i("kabbik_", "in home api token: $token")
            val response = resultFromExternalResponse { homeApiService.execute(token) }
            isLoadingCategory.value = false
            when (response) {
                is Success -> {
                    homeApiResponseCompose.value = response.data.data
                }
                
                is Failure -> {
                }
            }
        }
    }
    
    fun getAudioBookSeeMore(myTitle: String) {
        viewModelScope.launch {
            val response = resultFromResponse { audioBookSeeMoreService.execute(myTitle) }
            audioBookSeeMoreResponse.value = response
        }
    }
    
    fun getAudioBookEpisode(id: String) {
        viewModelScope.launch {
            val response = resultFromResponse { audioBookEpisodeListService.execute(id) }
            audioBookEpisodeResponse.value = response
            audioBookEpisodeResponseObserver.emit(response)
        }
    }
}