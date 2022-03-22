package com.banglalink.toffee.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.banglalink.toffee.data.database.entities.UserActivities

const val USER_ACTIVITIES_LIMIT = 150

@Dao
abstract class UserActivitiesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItem(item: UserActivities): Long

    @Update
    abstract suspend fun update(item: UserActivities)

    @Delete
    abstract suspend fun delete(item: UserActivities)

    @Query("DELETE FROM UserActivities")
    abstract suspend fun deleteAll()

    @Query("SELECT * from UserActivities WHERE customerId == :customerId ORDER BY id DESC")
    abstract fun getAllItems(customerId: Int): PagingSource<Int, UserActivities>
    
    @Query("DELETE FROM UserActivities WHERE customerId == :customerId AND channelId == :contentId")
    abstract suspend fun deleteByContentId(customerId: Int, contentId: Long)

    @Query("DELETE FROM UserActivities WHERE customerId=:customerId AND id NOT IN " +
            "(SELECT id from UserActivities WHERE customerId=:customerId ORDER BY updateTime DESC LIMIT "+ USER_ACTIVITIES_LIMIT +")")
    abstract suspend fun deleteExtraRows(customerId: Int)
    
    @Query("SELECT * FROM UserActivities WHERE channelId = :channelId AND type = :type")
    abstract suspend fun getUserActivityById(channelId: Long, type: String): UserActivities?
    
    @Query("UPDATE UserActivities SET payload = :payload WHERE channelId = :channelId AND type == :type")
    abstract suspend fun updateUserActivityPayload(channelId: Long, type: String, payload: String)

    @Transaction
    open suspend fun insert(item: UserActivities): Long {
        val ret = insertItem(item)
        deleteExtraRows(item.customerId)
        return ret
    }
}