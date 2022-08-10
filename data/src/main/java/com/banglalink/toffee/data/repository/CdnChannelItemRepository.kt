package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.CdnChannelItem

interface CdnChannelItemRepository {
    suspend fun insert(cdnChannelItem: CdnChannelItem): Long
    suspend fun delete(cdnChannelItem: CdnChannelItem): Int
    suspend fun update(cdnChannelItem: CdnChannelItem)
    suspend fun getAllCdnChannelItem(): List<CdnChannelItem>
    suspend fun deleteAllCdnChannelItem(): Int
    suspend fun getCdnChannelItemByChannelId(channelId: Long): CdnChannelItem?
    suspend fun updateCdnChannelItemByChannelId(channelId: Long, expiryDate: String?, payload: String)
    suspend fun deleteCdnChannelItemByChannelId(channelId: Long): Int
    suspend fun insertAll(vararg cdnChannelItemList: CdnChannelItem): LongArray
}