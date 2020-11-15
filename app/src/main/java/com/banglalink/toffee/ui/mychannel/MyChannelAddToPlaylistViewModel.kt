package com.banglalink.toffee.ui.mychannel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelAddToPlayListService
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.enums.ActivityType.PLAYLIST
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelAddToPlaylistBean
import com.banglalink.toffee.model.Resource
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MyChannelAddToPlaylistViewModel @ViewModelInject constructor(
    private val preference: Preference,
    private val activitiesRepo: UserActivitiesRepository,
    private val apiService: MyChannelAddToPlayListService
): ViewModel() {
    
    private val _data = MutableLiveData<Resource<MyChannelAddToPlaylistBean>>()
    val liveData = _data.toLiveData()
    
    fun addToPlaylist(playlistId: Int, contentId: Int, channelId: Int, isOwner: Int){
        viewModelScope.launch { 
            _data.postValue(resultFromResponse { apiService.invoke(playlistId, contentId, channelId, isOwner) })
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