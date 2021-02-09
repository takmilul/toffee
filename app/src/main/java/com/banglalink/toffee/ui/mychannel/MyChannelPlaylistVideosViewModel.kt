package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.apiservice.MyChannelPlaylistVideoDeleteService
import com.banglalink.toffee.apiservice.MyChannelPlaylistVideosService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.ActivityType.PLAYLIST
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelDeletePlaylistVideoBean
import com.banglalink.toffee.model.Resource
import com.google.gson.Gson
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class MyChannelPlaylistVideosViewModel @AssistedInject constructor(
    private val preference: Preference,
    private val activitiesRepo: UserActivitiesRepository,
    private val playlistVideoDeleteApiService: MyChannelPlaylistVideoDeleteService,
    private val apiService: MyChannelPlaylistVideosService.AssistedFactory,
    @Assisted var requestParams: MyChannelPlaylistContentParam
) :
    BasePagingViewModel<ChannelInfo>() {
    
    override val repo: BaseListRepository<ChannelInfo> by lazy {
        BaseListRepositoryImpl({ BaseNetworkPagingSource(apiService.create(requestParams)) })
    }
    private val _data = MutableLiveData<Resource<MyChannelDeletePlaylistVideoBean>>()
    val deletePlaylistVideoLiveData = _data.toLiveData()

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): MyChannelPlaylistVideosViewModel
    }

    companion object {
        fun provideAssisted(assistedFactory: AssistedFactory, requestParams: MyChannelPlaylistContentParam): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    
                    return assistedFactory.create(requestParams) as T
                }
            }
    }
    
    fun deletePlaylistVideo(channelId: Int, playlistContentId: Int, playlistId: Int){
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