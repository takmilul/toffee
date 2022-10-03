package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.ReactionStatusItem

interface ReactionCountRepository {
    suspend fun insert(item: ReactionStatusItem): Long
    suspend fun insertAll(vararg items: ReactionStatusItem): LongArray
    suspend fun getReactionStatusByChannelId(contentId: Long): List<ReactionStatusItem>?
    suspend fun getReactionCountByReactionType(contentId: Long, reactionType: Int): Long?
    suspend fun updateReaction(reactionStatusList: ArrayList<ReactionStatusItem>)
    suspend fun updateReaction(contentId: Long, reactionType: Int, status: Int): Int
}