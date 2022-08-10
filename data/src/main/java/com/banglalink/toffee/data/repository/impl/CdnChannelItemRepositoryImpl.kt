package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.CdnChannelItemDao
import com.banglalink.toffee.data.database.entities.CdnChannelItem
import com.banglalink.toffee.data.repository.CdnChannelItemRepository

class CdnChannelItemRepositoryImpl (private val dao: CdnChannelItemDao): CdnChannelItemRepository {
    override suspend fun insert(cdnChannelItem: CdnChannelItem): Long = dao.insert(cdnChannelItem)
    override suspend fun delete(cdnChannelItem: CdnChannelItem): Int = dao.delete(cdnChannelItem)
    override suspend fun update(cdnChannelItem: CdnChannelItem) = dao.update(cdnChannelItem)
    override suspend fun getAllCdnChannelItem(): List<CdnChannelItem> = dao.getAllCdnChannelItem()
    override suspend fun deleteAllCdnChannelItem(): Int = dao.deleteAllCdnChannelItem()
    override suspend fun getCdnChannelItemByChannelId(channelId: Long): CdnChannelItem? = dao.getCdnChannelItemByChannelId(channelId)
    override suspend fun updateCdnChannelItemByChannelId(channelId: Long, expiryDate: String?, payload: String) = dao
        .updateCdnChannelItemByChannelId(channelId, expiryDate, payload)
    override suspend fun deleteCdnChannelItemByChannelId(channelId: Long): Int = dao.deleteCdnChannelItemByChannelId(channelId)
    override suspend fun insertAll(vararg cdnChannelItemList: CdnChannelItem): LongArray = dao.insertAll(*cdnChannelItemList)
}