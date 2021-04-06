package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.ShareCount

interface ShareCountRepository {
    suspend fun insert(item: ShareCount): Int
    suspend fun insertAll(vararg items: ShareCount): LongArray
    suspend fun getShareCountByContentId(contentId: Int): Long?
    suspend fun updateShareCount(contentId: Int, status: Int): Int
}