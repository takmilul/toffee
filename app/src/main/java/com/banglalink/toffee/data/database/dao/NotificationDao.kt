package com.banglalink.toffee.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.banglalink.toffee.data.database.entities.NotificationInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    
    @Delete
    suspend fun delete(notificationInfo: NotificationInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: NotificationInfo): Long
    
    @Query("SELECT * FROM NotificationInfo WHERE userId=:userId OR userId=0 ORDER BY receiveTime DESC LIMIT 50")
    fun getAllNotification(userId: Int): PagingSource<Int, NotificationInfo>

    @Query("SELECT COUNT(id) FROM NotificationInfo WHERE (userId=:customerId OR userId=0) AND isSeen=:isSeen")
    fun getUnseenNotificationCount(customerId: Int, isSeen: Boolean = false): Flow<Int>

    /*@Query("SELECT * FROM NotificationInfo WHERE receiveTime >= :date ORDER BY receiveTime DESC")
    fun getNotificationByDate(date: Long): PagingSource<Int, NotificationInfo>
    
    @Query("SELECT * FROM NotificationInfo WHERE topic == :topic ORDER BY receiveTime DESC")
    fun getNotificationByTopic(topic: Int): PagingSource<Int, NotificationInfo>*/

    @Query("DELETE FROM NotificationInfo WHERE  userId=:customerId AND id NOT IN ( SELECT id FROM NotificationInfo where userId=:customerId ORDER BY id DESC LIMIT 5)")
    fun deleteExtraRows(customerId: Int)

    @Query("DELETE FROM NotificationInfo WHERE  userId=0 AND id NOT IN ( SELECT id FROM NotificationInfo  WHERE  userId=0 ORDER BY id DESC LIMIT 5)")
    fun deleteZeroUserRows()
    
    @Query("UPDATE NotificationInfo SET isSeen = :isSeen, seenTime = :seenTime WHERE id == :id")
    suspend fun updateSeenStatus(id: Long, isSeen: Boolean, seenTime: Long): Int

    @Transaction
     suspend fun insert(item: NotificationInfo): Long {
        val ret = insertItem(item)
        deleteExtraRows(item.userId)
        deleteZeroUserRows()
        return ret
    }
}