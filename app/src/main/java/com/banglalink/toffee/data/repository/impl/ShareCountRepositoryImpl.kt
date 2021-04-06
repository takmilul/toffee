package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ShareCountDao
import com.banglalink.toffee.data.database.entities.ShareCount
import com.banglalink.toffee.data.repository.ShareCountRepository

class ShareCountRepositoryImpl(private val dao: ShareCountDao) : ShareCountRepository {
    override suspend fun insert(item: ShareCount): Long {
        return dao.insert(item)
    }
    
    override suspend fun insertAll(vararg items: ShareCount): LongArray {
        return dao.insertAll(*items)
    }

    override suspend fun getShareCountByContentId(contentId: Int): Long? {
        return dao.getShareCountByContentId(contentId)
    }
    
    override suspend fun updateShareCount(contentId: Int, status: Int): Int {
        val count = getShareCountByContentId(contentId)
        
        return if(count == null){
            insert(ShareCount(contentId, 1)).toInt()
        }
        else{
            dao.updateShareCount(contentId, count + 1)
        }
    }
}