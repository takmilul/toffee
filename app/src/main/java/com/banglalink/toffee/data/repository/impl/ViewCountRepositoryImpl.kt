package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ViewCountDAO
import com.banglalink.toffee.data.database.entities.ViewCount
import com.banglalink.toffee.data.repository.ViewCountRepository

class ViewCountRepositoryImpl (private val dao: ViewCountDAO) : ViewCountRepository {
    override suspend fun insert(item: ViewCount): LongArray = dao.insertAll(item)
    override suspend fun delete(item: ViewCount) = dao.delete(item)
    override suspend fun update(item: ViewCount) = dao.update(item)
    override suspend fun getViewCountByChannelId(channelId: Int): Long? {
        return dao.getViewCountByChannelId(channelId)
    }
}