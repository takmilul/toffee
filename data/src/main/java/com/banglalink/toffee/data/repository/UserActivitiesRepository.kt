package com.banglalink.toffee.data.repository

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.entities.UserActivities

interface UserActivitiesRepository {
    suspend fun insert(item: UserActivities): Long
    suspend fun delete(item: UserActivities)
    suspend fun update(item: UserActivities)
    suspend fun deleteAll()
    suspend fun deleteByContentId(customerId: Int, contentId: Long)
    fun getAllItems(customerId: Int): PagingSource<Int, UserActivities>
    suspend fun getUserActivityById(channelId: Long, type: String): UserActivities?
    suspend fun updateUserActivityPayload(channelId: Long, type: String, payload: String)
}