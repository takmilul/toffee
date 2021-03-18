package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.apiservice.MyChannelPlaylistVideoDeleteService
import com.banglalink.toffee.apiservice.MyChannelPlaylistVideosService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.ActivityType.PLAYLIST
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelDeletePlaylistVideoBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyChannelPlaylistVideosViewModel @Inject constructor(
    private val preference: SessionPreference,
    private val activitiesRepo: UserActivitiesRepository,
    private val playlistVideoDeleteApiService: MyChannelPlaylistVideoDeleteService,
    private val apiService: MyChannelPlaylistVideosService.AssistedFactory,
) : BaseViewModel() {
    
    private val _data = SingleLiveEvent<Resource<MyChannelDeletePlaylistVideoBean>>()
    val deletePlaylistVideoLiveData = _data.toLiveData()
    
    fun getMyChannelPlaylistVideos(requestParams: MyChannelPlaylistContentParam): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                apiService.create(requestParams)
            )
        }).getList().cachedIn(viewModelScope)
    }
    
    fun deletePlaylistVideo(channelId: Int, playlistContentId: Int, playlistId: Int) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { playlistVideoDeleteApiService.invoke(channelId, playlistContentId, playlistId) })
        }
    }
    
    fun insertActivity(channelInfo: ChannelInfo, activitySubType: Int) {
        viewModelScope.launch {
            val item = UserActivities(
                preference.customerId,
                channelInfo.id.toLong(),
                "activity",
                channelInfo.type ?: "VOD",
                Gson().toJson(channelInfo),
                PLAYLIST.value,
                activitySubType
            )
            activitiesRepo.insert(item)
        }
    }
}