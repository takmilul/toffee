package com.banglalink.toffee.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.SendReactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ReactionViewModel @Inject constructor(
    private val json: Json,
    private val reactionDao: ReactionDao, 
    private val activitiesRepo: UserActivitiesRepository,
    private val sendReactionEvent: SendReactionEvent,
    ): ViewModel() {

    fun insertReaction(channelInfo: ChannelInfo, reactionInfo: ReactionInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = reactionDao.insert(reactionInfo)
            if (id > 0) {
                sendReactionEvent.execute(channelInfo, reactionInfo.copy(id = id), 1)
            }
        }
    }

    fun removeReaction(channelInfo: ChannelInfo, reactionInfo: ReactionInfo){
        viewModelScope.launch(Dispatchers.IO) {
            val id = reactionDao.delete(reactionInfo)
            if (id > 0) {
                sendReactionEvent.execute(channelInfo, reactionInfo, -1)
            }
        }
    }
    
    fun updateReaction(channelInfo: ChannelInfo, newReactionInfo: ReactionInfo, previousReactionInfo: ReactionInfo){
        viewModelScope.launch(Dispatchers.IO) {
            val id = reactionDao.updateReactionByContentId(newReactionInfo.customerId, newReactionInfo.contentId, newReactionInfo.reactionType)
            if (id > 0) {
                sendReactionEvent.execute(channelInfo, previousReactionInfo, -1)
                sendReactionEvent.execute(channelInfo, newReactionInfo, 1)
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
                json.encodeToString(channelInfo),
                reactStatus,
                reaction
            )
            activitiesRepo.insert(item)
        }
    }
}