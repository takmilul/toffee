package com.banglalink.toffee.data.repository.impl

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.dao.NotificationDao
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import kotlinx.coroutines.flow.Flow

class NotificationInfoRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val mPref: SessionPreference
): NotificationInfoRepository {
    override suspend fun insert(notificationInfo: NotificationInfo): Long {
        return notificationDao.insert(notificationInfo)
    }

    override suspend fun delete(notificationInfo: NotificationInfo) {
        notificationDao.delete(notificationInfo)
    }

    override fun getAllNotification(): PagingSource<Int, NotificationInfo> {
        return notificationDao.getAllNotification(mPref.customerId)
    }

    override fun getUnseenNotificationCount(): Flow<Int> {
        return notificationDao.getUnseenNotificationCount(mPref.customerId)
    }
    
    override suspend fun getLastNotification(): NotificationInfo? {
        return notificationDao.getLastNotification(mPref.customerId)
    }
    
    /*override suspend fun getNotificationByDate(date: Long): PagingSource<Int, NotificationInfo> {
        return notificationDao.getNotificationByDate(date)
    }

    override suspend fun getNotificationByTopic(topic: Int): PagingSource<Int, NotificationInfo> {
        return notificationDao.getNotificationByTopic(topic)
    }*/

    override suspend fun updateSeenStatus(id: Long, isSeen: Boolean, seenTime: Long): Int {
        return notificationDao.updateSeenStatus(id, isSeen, seenTime)
    }
}