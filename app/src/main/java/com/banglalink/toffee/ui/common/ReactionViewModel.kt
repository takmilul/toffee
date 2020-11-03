package com.banglalink.toffee.ui.common

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.enums.ActivityType.REACT
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ReactionViewModel @ViewModelInject constructor(private val reactionDao: ReactionDao, private val activitiesRepo: UserActivitiesRepository): ViewModel() {

    fun insert(reactionInfo: ReactionInfo) {
        viewModelScope.launch {
            reactionDao.insert(reactionInfo)
        }
    }

    fun insertActivity(customerId: Int, channelInfo: ChannelInfo, reactStatus: Int) {
        viewModelScope.launch {
            val item = UserActivities(
                customerId,
                channelInfo.id.toLong(),
                "activity",
                channelInfo.type ?: "VOD",
                Gson().toJson(channelInfo),
                REACT.value,
                reactStatus
            )
            activitiesRepo.insert(item)
        }
    }
}