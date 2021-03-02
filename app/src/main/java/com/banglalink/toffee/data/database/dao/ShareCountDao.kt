package com.banglalink.toffee.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.banglalink.toffee.data.database.entities.ShareCount

@Dao
interface ShareCountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg items: ShareCount): LongArray

    @Query("SELECT count FROM ShareCount WHERE contentId = :contentId LIMIT 1")
    suspend fun getShareCountByContentId(contentId: Int): Long?
}
