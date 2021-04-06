package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ReactionStatusDao
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.repository.ReactionStatusRepository

class ReactionStatusRepositoryImpl(private val dao: ReactionStatusDao): ReactionStatusRepository {
    override suspend fun insert(item: ReactionStatusItem): Long {
        return dao.insert(item)
    }
    
    override suspend fun insertAll(vararg items: ReactionStatusItem): LongArray {
        return dao.insertAll(*items)
    }

    override suspend fun getReactionStatusByChannelId(contentId: Long): List<ReactionStatusItem>? {
        return dao.getReactionStatusByChannelId(contentId)
    }
    
    override suspend fun getReactionCountByReactionType(contentId: Long, reactionType: Int): Long? {
        return dao.getReactionCountByReactionType(contentId, reactionType)
    }
    
    override suspend fun updateReaction(contentId: Long, reactionType: Int, status: Int): Int {
        val count = getReactionCountByReactionType(contentId, reactionType)
        
        return if(count == null) {
            status.takeIf { it > 0 }?.run {
                dao.insert(ReactionStatusItem(contentId.toInt(), reactionType, this.toLong())).toInt()
            } ?: 0
        }
        else{
            if(status == 1) {
                dao.updateReactionStatusByChannelId(contentId, reactionType, System.currentTimeMillis(), count + status)
            }
            else{
                count.takeIf { it > 0 }?.run {
                    dao.updateReactionStatusByChannelId(contentId, reactionType, System.currentTimeMillis(), this + status)
                } ?: 0
            }
        }
    }
}