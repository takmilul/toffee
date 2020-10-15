package com.banglalink.toffee.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.NotificationInfo

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notificationInfo: NotificationInfo)
    
    @Delete
    suspend fun delete(notificationInfo: NotificationInfo)
    
    @Query("SELECT * FROM NotificationInfo ORDER BY receiveTime DESC")
    fun getAllNotification(): PagingSource<Int, NotificationInfo>
    
    /*@Query("SELECT * FROM NotificationInfo WHERE receiveTime >= :date ORDER BY receiveTime DESC")
    fun getNotificationByDate(date: Long): PagingSource<Int, NotificationInfo>
    
    @Query("SELECT * FROM NotificationInfo WHERE topic == :topic ORDER BY receiveTime DESC")
    fun getNotificationByTopic(topic: Int): PagingSource<Int, NotificationInfo>*/
    
    @Query("UPDATE NotificationInfo SET isSeen = :isSeen, seenTime = :seenTime WHERE id == :id")
    suspend fun updateSeenStatus(id: Long, isSeen: Boolean, seenTime: Long): Int
}