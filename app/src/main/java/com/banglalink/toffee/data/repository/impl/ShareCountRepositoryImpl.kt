package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ShareCountDao
import com.banglalink.toffee.data.database.entities.ShareCount
import com.banglalink.toffee.data.repository.ShareCountRepository

class ShareCountRepositoryImpl(private val dao: ShareCountDao) : ShareCountRepository {
    override suspend fun insert(vararg items: ShareCount): LongArray {
        return dao.insert(*items)
    }

    override suspend fun getShareCountByContentId(contentId: Int): Long {
        return dao.getShareCountByContentId(contentId) ?: 0L
    }
}