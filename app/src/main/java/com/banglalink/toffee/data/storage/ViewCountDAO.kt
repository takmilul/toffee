package com.banglalink.toffee.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class ViewCountDAO{


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(vararg viewCountDataModelList: ViewCountDataModel): LongArray

    @Query("SELECT view_count FROM channel_view_count WHERE channel_id = :channelId")
    abstract fun getViewCountByChannelId(channelId:Int):Long?

}