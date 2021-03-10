package com.banglalink.toffee.data.repository

import com.banglalink.toffee.data.database.entities.ContentViewProgress

interface ContentViewPorgressRepsitory {
    suspend fun insert(item: ContentViewProgress)
    suspend fun delete(item: ContentViewProgress)
    suspend fun deleteByContentId(customerId: Int, contentId: Long)
    suspend fun getProgressByContent(contentId: Long): ContentViewProgress?
}