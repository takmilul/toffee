package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.ReactionStatusItem

interface ReactionStatusRepository {
    suspend fun insert(vararg items: ReactionStatusItem): LongArray
    suspend fun getReactionStatusByChannelId(channelId: Long): List<ReactionStatusItem>
}