package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.PlayerEventData

@Dao
interface PlayerEventsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlayerEventData): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: PlayerEventData): LongArray
    
    @Query("SELECT * FROM PlayerEventData ORDER BY id ASC LIMIT 30")
    fun getTopEventData(): List<PlayerEventData>?
    
    @Query("SELECT * FROM PlayerEventData ORDER BY id ASC")
    fun getAllRemainingEventData(): List<PlayerEventData>?
    
    @Query("DELETE FROM PlayerEventData where id IN (SELECT id FROM PlayerEventData ORDER BY id ASC LIMIT :limit)")
    suspend fun deleteTopEventData(limit: Int): Int
}