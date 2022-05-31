package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.banglalink.toffee.apiservice.*
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
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.SingleLiveEvent
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistVideosViewModel @Inject constructor(
    private val preference: SessionPreference,
    private val activitiesRepo: UserActivitiesRepository,
    private val apiService: MyChannelPlaylistVideosService.AssistedFactory,
    private val playlistVideoDeleteApiService: MyChannelPlaylistVideoDeleteService,
    private val playlistShareableService: PlaylistShareableService2.AssistedFactory,
    private val userPlaylistService: UserPlaylistVideosService.AssistedFactory,
) : ViewModel() {
    
    private val _data = SingleLiveEvent<Resource<MyChannelDeletePlaylistVideoBean>>()
    val deletePlaylistVideoLiveData = _data.toLiveData()
    
    fun getMyChannelPlaylistVideos(playlistInfo: PlaylistPlaybackInfo): Flow<PagingData<ChannelInfo>> {
        return if (playlistInfo.isFromShare) {
            BaseListRepositoryImpl({
                BaseNetworkPagingSource(
                    playlistShareableService.create(playlistInfo), ApiNames.GET_PLAYLIST_SHAREABLE, BrowsingScreens.HOME_PAGE
                )
            }).getList()
        } else {
            val requestParams = MyChannelPlaylistContentParam(playlistInfo.channelOwnerId, playlistInfo.playlistId)
            return BaseListRepositoryImpl({
                BaseNetworkPagingSource(
                    apiService.create(requestParams), ApiNames.GET_MY_CHANNEL_PLAYLIST_VIDEOS, BrowsingScreens.MY_CHANNEL_PLAYLIST_VIDEOS_PAGE
                )
            }).getList()
        }
    }
    
    fun getUserPlaylistVideos(playlistInfo: PlaylistPlaybackInfo): Flow<PagingData<ChannelInfo>> {
        return if (playlistInfo.isFromShare) {
            BaseListRepositoryImpl({
                BaseNetworkPagingSource(
                    playlistShareableService.create(playlistInfo), ApiNames.GET_PLAYLIST_SHAREABLE, BrowsingScreens.HOME_PAGE
                )
            }).getList()
        } else {
            val requestParams = MyChannelPlaylistContentParam(playlistInfo.channelOwnerId, playlistInfo.playlistId)
            return BaseListRepositoryImpl({
                BaseNetworkPagingSource(
                    userPlaylistService.create(requestParams), ApiNames.GET_USER_CHANNEL_PLAYLIST_VIDEOS, BrowsingScreens.USER_PLAYLIST_VIDEOS_PAGE
                )
            }).getList()
        }
    }
    
    fun deletePlaylistVideo(channelId: Int, playlistContentId: Int, playlistId: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { playlistVideoDeleteApiService.invoke(channelId, playlistContentId, playlistId) }
            _data.postValue(response)
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