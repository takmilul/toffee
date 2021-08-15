package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.SessionPrefData

@Dao
interface SessionPrefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg item: SessionPrefData)

    @Query("DELETE FROM SessionPrefData")
    suspend fun deleteAll()

    @Query("SELECT prefValue FROM SessionPrefData WHERE prefName=:key")
    suspend fun getPrefString(key: String): String?
}