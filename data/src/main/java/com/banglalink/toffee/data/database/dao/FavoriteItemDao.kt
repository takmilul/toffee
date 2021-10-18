package com.banglalink.toffee.data.database.dao

import androidx.room.*
import com.banglalink.toffee.data.database.entities.FavoriteItem

@Dao
interface FavoriteItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteItem): Long

    @Delete
    suspend fun delete(item: FavoriteItem)

    @Query("DELETE FROM FavoriteItem")
    suspend fun deleteAll()

    @Query("SELECT isFavorite from FavoriteItem WHERE channelId=:channelId")
    suspend fun isFavorite(channelId: Long): Int?
}