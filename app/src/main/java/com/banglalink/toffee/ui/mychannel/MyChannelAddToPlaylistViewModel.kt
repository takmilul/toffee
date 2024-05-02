package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelAddToPlayListService
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.ActivityType.PLAYLIST
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import com.banglalink.toffee.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class MyChannelAddToPlaylistViewModel @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
    private val activitiesRepo: UserActivitiesRepository,
    private val apiService: MyChannelAddToPlayListService,
) : ViewModel() {
    
    private val _data = MutableLiveData<Resource<MyChannelAddToPlaylistBean>>()
    val liveData = _data.toLiveData()
    
    fun addToPlaylist(playlistId: Int, contentId: Int, channelOwnerId: Int, isUserPlaylist:Int) {
        viewModelScope.launch {
            val response = resultFromResponse { apiService.invoke(playlistId, contentId, channelOwnerId, isUserPlaylist) }
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
                json.encodeToString(channelInfo),
                PLAYLIST.value,
                activitySubType
            )
            activitiesRepo.insert(item)
        }
    }
}