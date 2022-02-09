package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.database.entities.ViewCount

@Dao
interface  ViewCountDAO{
    @Update
    suspend fun update(item: ViewCount)

    @Delete
    suspend fun delete(item: ViewCount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg viewCountDataModelList: ViewCount): LongArray

    @Query("SELECT view_count FROM channel_view_count WHERE channel_id = :channelId")
    suspend fun getViewCountByChannelId(channelId:Int):Long?

}