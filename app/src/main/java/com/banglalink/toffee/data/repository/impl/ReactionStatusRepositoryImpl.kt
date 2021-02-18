package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ReactionStatusDao
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.repository.ReactionStatusRepository

class ReactionStatusRepositoryImpl(private val dao: ReactionStatusDao): ReactionStatusRepository {
    override suspend fun insert(vararg items: ReactionStatusItem): LongArray {
        return dao.insert(*items)
    }

    override suspend fun getReactionStatusByChannelId(channelId: Long): List<ReactionStatusItem> {
        return dao.getReactionStatusByChannelId(channelId)
    }
}