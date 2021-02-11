package com.banglalink.toffee.ui.common

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.SendReactionEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReactionViewModel @ViewModelInject constructor(
    private val reactionDao: ReactionDao, 
    private val activitiesRepo: UserActivitiesRepository,
    private val sendReactionEvent: SendReactionEvent,
    ): ViewModel() {

    fun insertReaction(reactionInfo: ReactionInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = reactionDao.insert(reactionInfo)
            if (id > 0) {
                sendReactionEvent.execute(reactionInfo.copy(id = id), 1)
            }
        }
    }

    fun removeReaction(reactionInfo: ReactionInfo){
        viewModelScope.launch(Dispatchers.IO) {
            val id = reactionDao.delete(reactionInfo)
            if (id > 0) {
                sendReactionEvent.execute(reactionInfo, -1)
            }
        }
    }
    
    fun updateReaction(newReactionInfo: ReactionInfo, previousReactionInfo: ReactionInfo){
        viewModelScope.launch(Dispatchers.IO) {
            val id = reactionDao.updateReactionByContentId(newReactionInfo.customerId, newReactionInfo.contentId, newReactionInfo.reactionType)
            if (id > 0) {
                sendReactionEvent.execute(previousReactionInfo, -1)
                sendReactionEvent.execute(newReactionInfo, 1)
            }
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