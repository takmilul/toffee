package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.ShareCount

interface ShareCountRepository {
    suspend fun insert(vararg items: ShareCount): LongArray
    suspend fun getShareCountByContentId(contentId: Int): Long
}