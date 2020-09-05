package com.banglalink.toffee.data.storage

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ChannelDAO: BaseDAO<ChannelDataModel> {

    @Transaction
    open fun saveChannel(channel: ChannelDataModel){
        insert(channel)
        deleteExtraRow()
    }
    @Query("SELECT * FROM `channels` ORDER BY modification_date DESC")
    abstract fun getAll(): List<ChannelDataModel>

    @Query("DELETE FROM channels where id NOT IN (SELECT id from channels ORDER BY modification_date DESC LIMIT 50)")
    abstract fun deleteExtraRow()

    @Query("DELETE FROM channels WHERE channel_id = :channelId")
    abstract fun deleteById(channelId: Int)



}