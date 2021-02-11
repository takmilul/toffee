package com.banglalink.toffee.data.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.banglalink.toffee.data.database.entities.UserActivities

@Dao
interface UserActivitiesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UserActivities): Long

    @Update
    suspend fun update(item: UserActivities)

    @Delete
    suspend fun delete(item: UserActivities)

    @Query("DELETE FROM UserActivities")
    suspend fun deleteAll()

    @Query("SELECT * from UserActivities WHERE customerId == :customerId ORDER BY id DESC")
    fun getAllItems(customerId: Int): PagingSource<Int, UserActivities>
    
    @Query("DELETE FROM UserActivities WHERE customerId == :customerId AND channelId == :contentId")
    suspend fun deleteByContentId(customerId: Int, contentId: Long)
}