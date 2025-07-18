package com.banglalink.toffee.data.repository.impl

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.dao.UserActivitiesDao
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository

class UserActivitiesRepositoryImpl(private val dao: UserActivitiesDao) : UserActivitiesRepository {
    override suspend fun insert(item: UserActivities): Long = dao.insert(item)
    override suspend fun delete(item: UserActivities) = dao.delete(item)
    override suspend fun update(item: UserActivities) = dao.update(item)
    override suspend fun deleteAll() =dao.deleteAll()
    override suspend fun deleteByContentId(customerId: Int, contentId: Long) = dao.deleteByContentId(customerId, contentId)
    override fun getAllItems(customerId: Int): PagingSource<Int, UserActivities> = dao.getAllItems(customerId)
    override suspend fun getUserActivityById(channelId: Long, type: String) = dao.getUserActivityById(channelId, type)
    override suspend fun getUserActivityListByType(type: String): List<UserActivities>? = dao.getUserActivityListByType(type)
    override suspend fun updateUserActivityPayload(channelId: Long, type: String, payload: String) = dao.updateUserActivityPayload(channelId, type, payload)
}