package com.banglalink.toffee.ui.audiobook

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.AudioBookEpisodeListService
import com.banglalink.toffee.apiservice.AudioBookSeeMoreService
import com.banglalink.toffee.apiservice.KabbikHomeApiService
import com.banglalink.toffee.apiservice.KabbikLoginApiService
import com.banglalink.toffee.data.network.response.AudioBookEpisodeResponse
import com.banglalink.toffee.apiservice.KabbikTopBannerApiService
import com.banglalink.toffee.data.network.response.KabbikHomeApiResponse
import com.banglalink.toffee.data.network.response.KabbikLoginApiResponse
import com.banglalink.toffee.data.network.response.AudioBookSeeMoreResponse
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse
import com.banglalink.toffee.data.network.util.resultFromExternalResponse
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.DateComparisonResult
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.compareDates
import com.banglalink.toffee.util.currentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AudioBookViewModel @Inject constructor(
    private val mPref: SessionPreference,
    @ApplicationContext private val appContext: Context,
    private val apiService: KabbikLoginApiService,
    private val homeApiService: KabbikHomeApiService,
    private val topBannerApiService: KabbikTopBannerApiService,
    private val audioBookSeeMoreService: AudioBookSeeMoreService,
    private val audioBookEpisodeListService: AudioBookEpisodeListService,
) : ViewModel() {
    val loginResponse = SingleLiveEvent<Resource<KabbikLoginApiResponse>>()
    val homeApiResponse = SingleLiveEvent<Resource<KabbikHomeApiResponse>>()
    val topBannerApiResponse = SingleLiveEvent<Resource<KabbikTopBannerApiResponse>>()
    val audioBookSeeMoreResponse = SingleLiveEvent<Resource<AudioBookSeeMoreResponse?>>()
    val audioBookEpisodeResponse = SingleLiveEvent<Resource<AudioBookEpisodeResponse?>>()

    fun grantToken(success:(token:String)->Unit, failure:()->Unit,) {
        viewModelScope.launch {
            val result = compareDates(
                fromDate = currentDateTime,
                toDate = mPref.kabbikTokenExpiryTime
            )
            when (result){
                DateComparisonResult.EARLIER->{
                    success.invoke(mPref.kabbikAccessToken)
                }
                else -> {
                    when(val loginResponse = resultFromExternalResponse { apiService.execute() }){
                        is Resource.Success->{
                            val systemDate =
                                Utils.dateToStr(Utils.getDate(Date().toString(), "yyyy-MM-dd")).toString()
                            mPref.kabbikTokenExpiryTime = systemDate + " " + loginResponse.data.expiry

                            loginResponse.data.token?.let {
                                mPref.kabbikAccessToken = it
                                success.invoke(it)
                            }
                        }
                        is Resource.Failure->{
                            appContext.showToast("Something went wrong. Please try again later.")
                            failure.invoke()
                        }
                    }
                }
            }
        }
    }

    fun homeApi(token: String){
        viewModelScope.launch {
            val response= resultFromExternalResponse {  homeApiService.execute(token) }
            homeApiResponse.value = response
        }
    }

    fun topBannerApi(token:String){
        viewModelScope.launch {
            val response = resultFromExternalResponse { topBannerApiService.execute(token) }
            topBannerApiResponse.value = response
        }
    }

    val homeApiResponseCompose = mutableStateOf(KabbikHomeApiResponse())
    fun homeApiCompose(token: String){
        viewModelScope.launch {
            val response = homeApiService.execute(token)
            homeApiResponseCompose.value = response
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
        }
    }
}