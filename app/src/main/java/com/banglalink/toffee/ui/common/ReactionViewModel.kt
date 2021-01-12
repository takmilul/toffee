package com.banglalink.toffee.ui.common

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ReactionViewModel @ViewModelInject constructor(private val reactionDao: ReactionDao, private val activitiesRepo: UserActivitiesRepository): ViewModel() {

    fun insertReaction(reactionInfo: ReactionInfo) {
        viewModelScope.launch {
            reactionDao.insert(reactionInfo)
        }
    }

    fun removeReaction(reactionInfo: ReactionInfo){
        viewModelScope.launch { 
            reactionDao.delete(reactionInfo)
        }
    }
    
    fun updateReaction(reactionInfo: ReactionInfo){
        viewModelScope.launch { 
            reactionDao.updateReactionByContentId(reactionInfo.customerId, reactionInfo.contentId, reactionInfo.reaction)
        }
    }
    
    fun insertActivity(customerId: Int, channelInfo: ChannelInfo, reactStatus: Int, reaction: Int) {
        viewModelScope.launch {
            val item = UserActivities(
                customerId,
                channelInfo.id.toLong(),
                "activity",
                channelInfo.type ?: "VOD",
                Gson().toJson(channelInfo),
                reactStatus,
                reaction
            )
            activitiesRepo.insert(item)
        }
    }
}