package com.banglalink.toffee.data.repository.impl

import com.banglalink.toffee.data.database.dao.ContentViewProgressDao
import com.banglalink.toffee.data.database.entities.ContentViewProgress
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.storage.SessionPreference

class ContentViewPorgressRepsitoryImpl(
    private val dao: ContentViewProgressDao,
    private val mPref: SessionPreference
    ): ContentViewPorgressRepsitory {
    
    override suspend fun insert(item: ContentViewProgress) {
        return dao.insert(item)
    }
    
    override suspend fun delete(item: ContentViewProgress) {
        return dao.delete(item)
    }

    override suspend fun deleteByContentId(customerId: Int, contentId: Long) = dao.deleteByContentId(customerId, contentId)
    
    override suspend fun getProgressByContent(contentId: Long): ContentViewProgress? {
        return dao.getProgressByContent(mPref.customerId, contentId)
    }
}