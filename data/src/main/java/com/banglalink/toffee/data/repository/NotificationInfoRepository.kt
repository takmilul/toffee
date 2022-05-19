package com.banglalink.toffee.data.repository

import androidx.paging.PagingSource
import com.banglalink.toffee.data.database.entities.NotificationInfo
import kotlinx.coroutines.flow.Flow

interface NotificationInfoRepository {
    suspend fun insert(notificationInfo: NotificationInfo): Long
    suspend fun delete(notificationInfo: NotificationInfo)
    fun getAllNotification(): PagingSource<Int, NotificationInfo>
    fun getUnseenNotificationCount(): Flow<Int>
    suspend fun getLastNotification(): NotificationInfo?
    /*suspend fun getNotificationByDate(date: Long): PagingSource<Int, NotificationInfo>
    suspend fun getNotificationByTopic(topic: Int): PagingSource<Int, NotificationInfo>*/
    suspend fun updateSeenStatus(id: Long, isSeen: Boolean, seenTime: Long): Int
}